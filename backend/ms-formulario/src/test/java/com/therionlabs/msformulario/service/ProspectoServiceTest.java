package com.therionlabs.msformulario.service;

import com.therionlabs.msformulario.dto.BatchSyncRequestDto;
import com.therionlabs.msformulario.dto.BatchSyncResponseDto;
import com.therionlabs.msformulario.dto.CarreraPreferenciaDto;
import com.therionlabs.msformulario.dto.DashboardResumenDto;
import com.therionlabs.msformulario.dto.ProspectoRequestDto;
import com.therionlabs.msformulario.dto.ProspectoResponseDto;
import com.therionlabs.msformulario.exception.ApiException;
import com.therionlabs.msformulario.mapper.ProspectoMapper;
import com.therionlabs.msformulario.model.Prospecto;
import com.therionlabs.msformulario.model.enums.Genero;
import com.therionlabs.msformulario.model.enums.Jornada;
import com.therionlabs.msformulario.model.enums.NivelInteres;
import com.therionlabs.msformulario.model.enums.PerfilMotivacional;
import com.therionlabs.msformulario.model.enums.SituacionAcademica;
import com.therionlabs.msformulario.repository.ProspectoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProspectoServiceTest {

    @Mock
    private ProspectoRepository prospectoRepository;

    @Mock
    private ProspectoSyncItemProcessor syncItemProcessor;

    private ProspectoService prospectoService;

    @BeforeEach
    void setUp() {
        prospectoService = new ProspectoService(prospectoRepository, new ProspectoMapper(), syncItemProcessor);
    }

    @Test
    void crearLanzaConflict409CuandoRutYaExiste() {
        ProspectoRequestDto request = sampleDto("uuid-1", "111111111");
        when(prospectoRepository.existsByRut("11.111.111-1")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> prospectoService.crear(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void crearGuardaYNormalizaRutCuandoEsValido() {
        ProspectoRequestDto request = sampleDto("uuid-1", "111111111");
        when(prospectoRepository.existsByRut("11.111.111-1")).thenReturn(false);
        when(prospectoRepository.existsById("uuid-1")).thenReturn(false);
        when(prospectoRepository.saveAndFlush(any(Prospecto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProspectoResponseDto response = prospectoService.crear(request);
        assertEquals("11.111.111-1", response.rut());
        assertEquals("uuid-1", response.clientUuid());
    }

    @Test
    void obtenerLanzaNotFound404CuandoNoExiste() {
        when(prospectoRepository.findById("no-existe")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> prospectoService.obtener("no-existe"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void actualizarLanzaConflict409SiNuevoRutPerteneceAOtroProspecto() {
        Prospecto actual = Prospecto.builder()
                .clientUuid("uuid-1")
                .rut("11.111.111-1")
                .genero(Genero.FEMENINO)
                .situacionAcademica(SituacionAcademica.CUARTO_MEDIO)
                .jornadaPreferencia(Jornada.DIURNA)
                .nivelInteres(NivelInteres.ALTO)
                .perfilMotivacional(PerfilMotivacional.INVERSION)
                .build();
        Prospecto otro = Prospecto.builder().clientUuid("uuid-2").rut("11.111.112-K").build();

        when(prospectoRepository.findById("uuid-1")).thenReturn(Optional.of(actual));
        when(prospectoRepository.findByRut("11.111.112-K")).thenReturn(Optional.of(otro));

        ApiException ex = assertThrows(ApiException.class,
                () -> prospectoService.actualizar("uuid-1", sampleDto("uuid-1", "11111112k")));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void sincronizarLoteRetornaExitosYFallosParcialesSinAbortar() {
        ProspectoRequestDto ok = sampleDto("uuid-ok", "11.111.111-1");
        ProspectoRequestDto bad = sampleDto("uuid-bad", "11.111.111-9");

        when(syncItemProcessor.procesarItem(ok)).thenReturn("uuid-ok");
        when(syncItemProcessor.procesarItem(bad)).thenThrow(ApiException.badRequest("RUT inválido"));

        BatchSyncResponseDto result = prospectoService.sincronizarLote(
                new BatchSyncRequestDto(2, "EVT-1", List.of(ok, bad)));

        assertEquals(2, result.totalProcesados());
        assertEquals(List.of("uuid-ok"), result.exitososUuids());
        assertEquals(List.of("uuid-bad"), result.fallidosUuids());
    }

    @Test
    void obtenerResumenAgregaConteosCorrectamente() {
        when(prospectoRepository.countByOptionalEventoId("EVT-1")).thenReturn(5L);
        when(prospectoRepository.countGroupedBySede("EVT-1"))
                .thenReturn(List.<Object[]>of(new Object[]{"Viña del Mar", 3L}, new Object[]{"Valparaíso", 2L}));
        when(prospectoRepository.countGroupedByNivelInteres("EVT-1"))
                .thenReturn(List.<Object[]>of(new Object[]{NivelInteres.ALTO, 4L}));
        when(prospectoRepository.countGroupedByJornada("EVT-1"))
                .thenReturn(List.<Object[]>of(new Object[]{Jornada.DIURNA, 5L}));

        DashboardResumenDto resumen = prospectoService.obtenerResumen("EVT-1");

        assertEquals(5L, resumen.totalProspectos());
        assertEquals(3L, resumen.conteoPorSede().get("Viña del Mar"));
        assertEquals(4L, resumen.conteoPorNivelInteres().get("ALTO"));
        assertEquals(5L, resumen.conteoPorJornada().get("DIURNA"));
    }

    @Test
    void listarNormalizaFiltrosYDevuelveResultados() {
        Prospecto p = Prospecto.builder()
                .clientUuid("uuid-1")
                .captadorId(2)
                .eventoId("EVT-1")
                .rut("11.111.111-1")
                .nombres("Camila")
                .apellidos("Valenzuela")
                .genero(Genero.FEMENINO)
                .situacionAcademica(SituacionAcademica.CUARTO_MEDIO)
                .jornadaPreferencia(Jornada.DIURNA)
                .nivelInteres(NivelInteres.ALTO)
                .perfilMotivacional(PerfilMotivacional.VOCACIONAL)
                .build();

        when(prospectoRepository.findFiltered(2, "EVT-1", "11.111.111-1", "Viña del Mar"))
                .thenReturn(List.of(p));
        when(prospectoRepository.findFiltered(null, null, null, null))
                .thenReturn(List.of());

        List<ProspectoResponseDto> conFiltros = prospectoService.listar(2, " EVT-1 ", "111111111", " Viña del Mar ");
        assertEquals(1, conFiltros.size());
        assertEquals("uuid-1", conFiltros.get(0).clientUuid());

        List<ProspectoResponseDto> sinFiltros = prospectoService.listar(null, "  ", "", null);
        assertEquals(0, sinFiltros.size());
    }

    @Test
    void obtenerRetornaDtoCuandoExiste() {
        Prospecto p = Prospecto.builder()
                .clientUuid("uuid-1")
                .rut("11.111.111-1")
                .genero(Genero.FEMENINO)
                .situacionAcademica(SituacionAcademica.CUARTO_MEDIO)
                .jornadaPreferencia(Jornada.DIURNA)
                .nivelInteres(NivelInteres.ALTO)
                .perfilMotivacional(PerfilMotivacional.VOCACIONAL)
                .build();
        when(prospectoRepository.findById("uuid-1")).thenReturn(Optional.of(p));

        ProspectoResponseDto res = prospectoService.obtener("uuid-1");
        assertEquals("uuid-1", res.clientUuid());
    }

    @Test
    void crearValidaRutInvalidoYClientUuidDuplicadoYClientUuidVacio() {
        ProspectoRequestDto rutMalo = sampleDto("uuid-1", "11.111.111-9");
        ApiException exRut = assertThrows(ApiException.class, () -> prospectoService.crear(rutMalo));
        assertEquals(HttpStatus.BAD_REQUEST, exRut.getStatus());

        ProspectoRequestDto uuidDup = sampleDto(" uuid-dup ", "111111111");
        when(prospectoRepository.existsByRut("11.111.111-1")).thenReturn(false);
        when(prospectoRepository.existsById("uuid-dup")).thenReturn(true);
        ApiException exUuid = assertThrows(ApiException.class, () -> prospectoService.crear(uuidDup));
        assertEquals(HttpStatus.CONFLICT, exUuid.getStatus());

        ProspectoRequestDto sinUuid = sampleDto(null, "111111111");
        when(prospectoRepository.saveAndFlush(any(Prospecto.class))).thenAnswer(inv -> {
            Prospecto saved = inv.getArgument(0);
            saved.setClientUuid("generated-uuid");
            return saved;
        });
        assertEquals("generated-uuid", prospectoService.crear(sinUuid).clientUuid());

        ProspectoRequestDto blancoUuid = sampleDto("  ", "111111111");
        assertEquals("generated-uuid", prospectoService.crear(blancoUuid).clientUuid());
    }

    @Test
    void actualizarPermiteMismoRutORutNuevoYRechazaNoExistenteORutInvalido() {
        Prospecto actual = Prospecto.builder()
                .clientUuid("uuid-1")
                .rut("11.111.111-1")
                .genero(Genero.FEMENINO)
                .situacionAcademica(SituacionAcademica.CUARTO_MEDIO)
                .jornadaPreferencia(Jornada.DIURNA)
                .nivelInteres(NivelInteres.ALTO)
                .perfilMotivacional(PerfilMotivacional.INVERSION)
                .build();

        when(prospectoRepository.findById("no-existe")).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ApiException.class,
                        () -> prospectoService.actualizar("no-existe", sampleDto("no-existe", "111111111"))).getStatus());

        when(prospectoRepository.findById("uuid-1")).thenReturn(Optional.of(actual));
        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(ApiException.class,
                        () -> prospectoService.actualizar("uuid-1", sampleDto("uuid-1", "111111119"))).getStatus());

        when(prospectoRepository.findByRut("11.111.111-1")).thenReturn(Optional.of(actual));
        when(prospectoRepository.saveAndFlush(any(Prospecto.class))).thenAnswer(inv -> inv.getArgument(0));
        assertEquals("11.111.111-1",
                prospectoService.actualizar("uuid-1", sampleDto("uuid-1", "111111111")).rut());

        when(prospectoRepository.findByRut("11.111.112-K")).thenReturn(Optional.empty());
        assertEquals("11.111.112-K",
                prospectoService.actualizar("uuid-1", sampleDto("uuid-1", "11111112k")).rut());
    }

    @Test
    void eliminarBorraCuandoExisteYLanza404CuandoNoExiste() {
        Prospecto actual = Prospecto.builder().clientUuid("uuid-1").build();
        when(prospectoRepository.findById("uuid-1")).thenReturn(Optional.of(actual));
        prospectoService.eliminar("uuid-1");
        org.mockito.Mockito.verify(prospectoRepository).delete(actual);

        when(prospectoRepository.findById("missing")).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ApiException.class, () -> prospectoService.eliminar("missing")).getStatus());
    }

    @Test
    void sincronizarLoteCubreExitoTotalItemNuloYSinUuidYExcepcionSinMensaje() {
        ProspectoRequestDto ok = sampleDto("uuid-ok", "111111111");
        when(syncItemProcessor.procesarItem(ok)).thenReturn("uuid-ok");

        BatchSyncResponseDto todoOk = prospectoService.sincronizarLote(
                new BatchSyncRequestDto(2, "EVT-1", List.of(ok)));
        assertEquals("Sincronización completada exitosamente.", todoOk.mensaje());

        ProspectoRequestDto sinUuid = sampleDto(null, "111111111");
        when(syncItemProcessor.procesarItem(null)).thenThrow(new RuntimeException((String) null));
        when(syncItemProcessor.procesarItem(sinUuid)).thenThrow(new IllegalStateException("fallo"));

        List<ProspectoRequestDto> items = new java.util.ArrayList<>();
        items.add(null);
        items.add(sinUuid);

        BatchSyncResponseDto parcial = prospectoService.sincronizarLote(
                new BatchSyncRequestDto(2, "EVT-1", items));
        assertEquals(2, parcial.fallidosUuids().size());
        assertEquals("SIN_UUID", parcial.fallidosUuids().get(0));
        assertEquals("fallo", parcial.erroresPorUuid().get("SIN_UUID"));
    }

    @Test
    void obtenerResumenConEventoBlancoYNuloYFilasConClaveNula() {
        when(prospectoRepository.countByOptionalEventoId(null)).thenReturn(1L);
        when(prospectoRepository.countGroupedBySede(null))
                .thenReturn(List.<Object[]>of(new Object[]{null, 1L}));
        when(prospectoRepository.countGroupedByNivelInteres(null)).thenReturn(List.of());
        when(prospectoRepository.countGroupedByJornada(null)).thenReturn(List.of());

        DashboardResumenDto resumenBlanco = prospectoService.obtenerResumen("   ");
        assertEquals(1L, resumenBlanco.totalProspectos());
        assertEquals(1L, resumenBlanco.conteoPorSede().get("SIN_DEFINIR"));

        DashboardResumenDto resumenNulo = prospectoService.obtenerResumen(null);
        assertEquals(1L, resumenNulo.totalProspectos());
    }

    private ProspectoRequestDto sampleDto(String uuid, String rut) {
        return new ProspectoRequestDto(
                uuid, 2, "EVT-1", 1728834720000L,
                rut, "Camila", "Valenzuela", "FEMENINO", "2008-04-15",
                "Quilpué", "+56987654321", "c.valenzuela@gmail.com", true,
                "CUARTO_MEDIO", null, "Colegio Alemán", "Viña del Mar",
                "DIURNA", "Viña del Mar", "ALTO", "VOCACIONAL",
                List.of(new CarreraPreferenciaDto(1, "Diseño Gráfico"))
        );
    }
}

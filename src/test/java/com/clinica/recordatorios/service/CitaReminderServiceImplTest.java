package com.clinica.recordatorios.service;

import com.clinica.recordatorios.channel.ReminderChannel;
import com.clinica.recordatorios.channel.ReminderChannelFactory;
import com.clinica.recordatorios.domain.CanalNotificacion;
import com.clinica.recordatorios.domain.Cita;
import com.clinica.recordatorios.repository.CitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaReminderServiceImplTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private ReminderChannelFactory reminderChannelFactory;

    @Mock
    private ReminderChannel reminderChannelMock;

    // Fake, no Mock: Clock.fixed(...) es una implementación real y completa de
    // java.time.Clock; LocalDateTime.now(clock) funciona igual que en producción,
    // solo que con el instante congelado.
    private final Clock clockFijo =
        Clock.fixed(Instant.parse("2026-08-10T09:00:00Z"), ZoneId.of("UTC"));

    private CitaReminderServiceImpl citaReminderService;

    @BeforeEach
    void setUp() {
        citaReminderService =
            new CitaReminderServiceImpl(citaRepository, reminderChannelFactory, clockFijo);
    }

    @Test
    void enviarRecordatoriosPendientes_citaEnVentana_usaElCanalPreferido() {
        Cita cita = new Cita(1L, "Ana Gómez", "ana@correo.com",
            LocalDateTime.of(2026, 8, 10, 15, 0), CanalNotificacion.EMAIL, false);

        when(citaRepository.findByFechaHoraCitaBetweenAndRecordatorioEnviadoFalse(any(), any()))
            .thenReturn(List.of(cita));
        when(reminderChannelFactory.obtenerCanal(CanalNotificacion.EMAIL))
            .thenReturn(reminderChannelMock);

        int enviados = citaReminderService.enviarRecordatoriosPendientes();

        assertEquals(1, enviados);
        verify(reminderChannelMock, times(1)).enviar(eq(cita), anyString());
        verify(citaRepository, times(1)).save(cita);
        assertTrue(cita.isRecordatorioEnviado());
    }

    @Test
    void enviarRecordatoriosPendientes_citasConCanalesDistintos_pideACadaCitaSuCanalPreferido() {
        Cita citaEmail = new Cita(2L, "Ana Gómez", "ana@correo.com",
            LocalDateTime.of(2026, 8, 10, 15, 0), CanalNotificacion.EMAIL, false);
        Cita citaSms = new Cita(3L, "Luis Peña", "3001234567",
            LocalDateTime.of(2026, 8, 11, 8, 0), CanalNotificacion.SMS, false);
        ReminderChannel canalSmsMock = mock(ReminderChannel.class);

        when(citaRepository.findByFechaHoraCitaBetweenAndRecordatorioEnviadoFalse(any(), any()))
            .thenReturn(List.of(citaEmail, citaSms));
        when(reminderChannelFactory.obtenerCanal(CanalNotificacion.EMAIL)).thenReturn(reminderChannelMock);
        when(reminderChannelFactory.obtenerCanal(CanalNotificacion.SMS)).thenReturn(canalSmsMock);

        int enviados = citaReminderService.enviarRecordatoriosPendientes();

        assertEquals(2, enviados);
        verify(reminderChannelMock).enviar(eq(citaEmail), contains("2026-08-10T15:00"));
        verify(canalSmsMock).enviar(eq(citaSms), contains("2026-08-11T08:00"));
        verify(reminderChannelMock, never()).enviar(eq(citaSms), anyString());
    }

    @Test
    void enviarRecordatoriosPendientes_ventanaDeterministaSegunClockFijo_consultaLosLimitesExactos() {
        when(citaRepository.findByFechaHoraCitaBetweenAndRecordatorioEnviadoFalse(any(), any()))
            .thenReturn(List.of());

        citaReminderService.enviarRecordatoriosPendientes();

        ArgumentCaptor<LocalDateTime> desdeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> hastaCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(citaRepository).findByFechaHoraCitaBetweenAndRecordatorioEnviadoFalse(
            desdeCaptor.capture(), hastaCaptor.capture());

        // Con Clock.systemDefaultZone() esta aserción cambiaría en cada ejecución;
        // con el Fake el resultado es exacto en cualquier máquina y a cualquier hora.
        assertEquals(LocalDateTime.of(2026, 8, 10, 9, 0), desdeCaptor.getValue());
        assertEquals(LocalDateTime.of(2026, 8, 11, 9, 0), hastaCaptor.getValue());
    }

    @Test
    void enviarRecordatoriosPendientes_sinCitasPendientes_noConsultaLaFactory() {
        when(citaRepository.findByFechaHoraCitaBetweenAndRecordatorioEnviadoFalse(any(), any()))
            .thenReturn(List.of());

        int enviados = citaReminderService.enviarRecordatoriosPendientes();

        assertEquals(0, enviados);
        verifyNoInteractions(reminderChannelFactory);
        verify(citaRepository, never()).save(any());
    }
}

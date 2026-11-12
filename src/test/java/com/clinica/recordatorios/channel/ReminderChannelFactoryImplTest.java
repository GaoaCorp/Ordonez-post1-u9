package com.clinica.recordatorios.channel;

import com.clinica.recordatorios.channel.gateway.NotificadorSmsGateway;
import com.clinica.recordatorios.domain.CanalNotificacion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class ReminderChannelFactoryImplTest {

    // Sin Mockito para las estrategias: son objetos reales y baratos de construir.
    // El gateway sí es un Dummy: la fábrica nunca lo invoca.
    private final EmailReminderChannel email = new EmailReminderChannel();
    private final SmsReminderChannel sms = new SmsReminderChannel(mock(NotificadorSmsGateway.class));
    private final ReminderChannelFactoryImpl factory = new ReminderChannelFactoryImpl(email, sms);

    @Test
    void obtenerCanal_email_retornaEstrategiaDeCorreo() {
        assertSame(email, factory.obtenerCanal(CanalNotificacion.EMAIL));
    }

    @Test
    void obtenerCanal_sms_retornaEstrategiaDeSms() {
        assertSame(sms, factory.obtenerCanal(CanalNotificacion.SMS));
    }
}

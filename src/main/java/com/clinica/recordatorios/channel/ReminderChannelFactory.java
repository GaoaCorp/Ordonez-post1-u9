package com.clinica.recordatorios.channel;

import com.clinica.recordatorios.domain.CanalNotificacion;

/** Factory Method: decide qué estrategia de envío entregar según el canal. */
public interface ReminderChannelFactory {
    ReminderChannel obtenerCanal(CanalNotificacion tipo);
}

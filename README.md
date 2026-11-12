# Recordatorios Citas Service

## Descripción
Repositorio del post-contenido de la Unidad 9 de Patrones de Diseño de Software (sexto semestre). Es un proyecto Spring Boot que envía recordatorios de citas por el canal preferido del paciente. Desde el inicio está construido con Strategy (`ReminderChannel`) y Factory Method (`ReminderChannelFactory`), inyectados por constructor.

## Parte 1 — Pruebas unitarias con JUnit 5 y Mockito
`CitaReminderServiceImplTest` aísla `CitaReminderServiceImpl` de sus tres colaboradores.

## Decisiones de diseño
- **CitaRepository → Mock.** La prueba controla la respuesta de la consulta y además verifica que `save()` se llame exactamente una vez con la cita marcada como enviada.
- **ReminderChannelFactory / ReminderChannel → Mock.** Lo que se prueba es la selección de la estrategia, y eso solo se comprueba verificando la interacción.
- **Clock → Fake (`Clock.fixed`).** Es una implementación real del JDK con el instante congelado, más simple y más fiel que un mock de `Clock`.

## Ejecutar las pruebas
```bash
mvn test
```

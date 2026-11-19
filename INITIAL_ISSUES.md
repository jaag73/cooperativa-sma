# Initial Issues (Fase 1: Preparación para Producción)

Las siguientes tareas componen nuestro *Sprint* hacia la estabilización para uso público y están listas para ser reclamadas por desarrolladores colaboradores.

---

### [TASK] Desglose transparente de tarifa y cuota cooperativa en `CartCheckoutScreen`
- **Etiquetas:** `enhancement`, `good first issue`, `phase-1-launch`
- **Descripción:** Actualmente la pantalla de cobro muestra un "MockDeliveryFee" opaco. Debemos separar esto en la UI y enseñar al usuario explícitamente cuánto se lleva el repartidor, y la mínima aportación para la cooperativa. (Requiere actualización del `CartCheckoutViewModel`).

### [TASK] Reglas de seguridad `firestore.rules` listas para despliegue en producción
- **Etiquetas:** `security`, `help wanted`, `phase-1-launch`
- **Descripción:** Evitar inyecciones en la base de datos de producción. Crear reglas robustas de Firestore para que un negocio no pueda alterar pedidos ajenos, o el cliente no modifique su `OrderLifecycleStatus` directamente.

### [TASK] Notificaciones Push (FCM) ante cambios de estado de pedido
- **Etiquetas:** `feature`, `core-mechanic`, `future-bounty-eligible`
- **Descripción:** Actualmente disparamos alertas de sistema local (*NotificationManagerCompat*). Debemos mutar el servicio para enlazar Firebase Cloud Messaging. El Repositorio debe emitir el Push real hacia el cliente cuando el repartidor presione `startTransitToClient()`.

### [TASK] Animación y confirmación háptica al validar el PIN de 4 dígitos
- **Etiquetas:** `ui-ux`, `good first issue`, `phase-1-launch`
- **Descripción:** La pantalla del Repartidor (`CourierActiveDeliveryScreen`) valida el PIN correctamente, pero necesita dar mejor respuesta táctil. Añadir feedback háptico (vibración) de éxito/fracaso y una breve animación Lottie de "viaje terminado".

### [TASK] Filtros por categoría y búsqueda en tiempo real en `ClientHomeScreen`
- **Etiquetas:** `feature`, `compose`
- **Descripción:** El cliente actual solo ve negocios globales. Falta una barra superior (`SearchBar` de Material 3) para buscar por nombre de producto y filtros por `BusinessTaxonomy`. 

### [TASK] Exportación de corte de caja diario en CSV/PDF para el Micronegocio
- **Etiquetas:** `feature`, `business-logic`, `future-bounty-eligible`
- **Descripción:** El negocio puede ver lo que vendió hoy en la pestaña de *Finanzas*, pero necesita un botón que genere localmente un archivo `.csv` (mediante Intents) o `.pdf` detallando ticket por ticket para su contabilidad.

### [TASK] Auditoría de contraste WCAG y soporte completo de Modo Oscuro en `CourierDashboardScreen`
- **Etiquetas:** `ui-ux`, `accessibility`, `good first issue`
- **Descripción:** El repartidor usa la app bajo el sol intenso. Revisar los colores `AgaveGreen` vs `Terracotta` contra directrices de accesibilidad AA/AAA para el modo oscuro en particular.

### [TASK] Diseño del módulo base para Vertical 2: Servicios y Oficios Locales
- **Etiquetas:** `architecture`, `discussion`, `future-bounty-eligible`
- **Descripción:** Abrir el documento de diseño inicial para que, sin romper el ecosistema del "Delivery", extendamos perfiles a *Plomeros/Técnicos* en la plataforma de San Miguel de Allende. (Esta es una tarea exploratoria).

# Guía de Contribución

¡Gracias por tu interés en colaborar con Cooperativa SMA! Construir esta red solidaria exige altos estándares técnicos en Android para que la operación diaria no falle en manos de los vecinos, negocios y repartidores.

## Configuración del Entorno Local

1. **Clonar Repositorio:**
   ```bash
   git clone https://github.com/TUREPOS/CooperativaDeliveryApp.git
   ```
2. **Requisitos Mínimos:**
   - Android Studio (Koala Feature Drop o superior).
   - Java JDK 17 (mínimo compatible con nuestro AGP y Kotlin).
   - Emulador Android (API 34+).
3. **Flujo Local (Firebase Emulator Suite):**
   Es obligatorio testear localmente sin ensuciar la base de datos de producción. Revisa el **README.md** para levantar el emulador en el puerto 8080/9099 usando el comando `adb reverse`.

## Reglas de Arquitectura e Implementación

Para ser aceptado un Pull Request, debes seguir esta estructura:
- **Clean Architecture + MVVM:** Mantén estricta separación entre capas (`data`, `domain`, `presentation`).
- **UDF (Unidirectional Data Flow):** La UI de Compose solo reacciona al `StateFlow` del ViewModel. El ViewModel maneja eventos mediante sellados (sealed classes `UiEvent`). No mezcles estado en la UI.
- **Jetpack Compose (Material 3):** No se admite XML. Reutiliza los componentes centrales (`PrimaryActionButton`, `StatusStepper`) ubicados en `ui/components/`. Usa nuestros esquemas semánticos de colores (`Terracotta`, `AgaveGreen`).
- **Inyección de Dependencias:** Todo repositorio y ViewModel debe estar integrado al árbol con **Dagger Hilt** y proveído a través de interfaces (`RepositoryModule`, `FirebaseModule`).

## Flujo de Trabajo con Issues

1. **Asignación:** Busca un Issue libre en el tablero, comenta que lo quieres tomar (si tiene etiqueta `help wanted` o `good first issue`).
2. **Branching:** Crea tu rama partiendo de `main` con la convención:
   - `feature/ISSUE-ID-nombre` (Ej. `feature/12-animacion-pin`)
   - `bugfix/ISSUE-ID-nombre`
3. **Commits Limpios:** Sigue la convención [Conventional Commits](https://www.conventionalcommits.org/).

## Checklist antes de enviar un Pull Request
Antes de presionar "Crear PR", audita localmente:
- [ ] Tu código compila sin errores ni *warnings* residuales de Hilt o Compose (`./gradlew assembleDebug`).
- [ ] El código no rompe la máquina de estados de `OrderLiveRepositoryImpl` ni desincroniza al emulador local.
- [ ] Todo texto de la UI ha sido extraído a cadenas en recursos (`strings.xml`) **en español de México** y es semánticamente amistoso.
- [ ] (Opcional pero valorado) Realizaste validaciones unitarias o actualizaste el `LocalEcosystemAuditTest`.

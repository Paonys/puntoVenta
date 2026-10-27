# AGENTS.md

Punto de Venta "Novedades Ale": JavaFX 21 desktop app (Maven, FXML, SQLite, JUnit 5). Code, comments, and all docs are in **Spanish** — keep identifiers and messages in Spanish.

## Layout / where to run commands

The Maven project is **not at the repo root** — it lives in `papeleriaAleJavaFX/`. Run every Maven command with that as the working directory (e.g. `workdir=papeleriaAleJavaFX`). There is no wrapper script; use the system `mvn`.

```
puntoVenta/
├── docs/                     status reports + 2-agent work plan (Spanish, read these first)
└── papeleriaAleJavaFX/
    ├── pom.xml               project config + design tokens
    └── src/main/
        ├── java/com/novedadesale/puntoventa/
        │   ├── App.java      JavaFX entrypoint (loads LoginPage -> MainLayout)
        │   ├── controllers/  FXML controllers
        │   ├── models/       immutable records + enums
        │   ├── repositories/ interfaces + repositories/sqlite/ DAOs
        │   ├── services/     interfaces + services/impl/  (hidden behind Servicios)
        │   ├── persistence/  BaseDatos, Esquema (DDL), Seeder
        │   ├── shared/       AppConstants, CalculadoraTotales, Formato, Reloj, Seguridad, Validadores
        │   ├── ui/           Sesion, Dialogos, Componentes, Animations, TemporizadorInactividad
        │   └── exceptions/   ServiceException, ValidacionException
        └── resources/com/novedadesale/puntoventa/
            ├── styles/       8 CSS files (atoms/molecules/organisms/pages/templates/theme/utilities/animations)
            └── views/        pages/*.fxml (11) + templates/MainLayout.fxml
```

Runtime FXML/CSS live **only** under `src/main/resources`; do not add copies next to Java.

## Commands (from `papeleriaAleJavaFX/`)

- Build: `mvn -B clean compile`
- Run the app: `mvn javafx:run` (mainClass `com.novedadesale.puntoventa.App`)
- All tests: `mvn -B test` (61 tests across `SharedTest`, `PersistenciaTest`, `NegocioTest`, `AutorizacionTest`, `CredencialesTest`, `InactividadTest`)
- Single test class: `mvn -B test -Dtest=NegocioTest`
- Single test method: `mvn -B test -Dtest=NegocioTest#totalesSinDescuentoAplicanIva16`

There is no linter/formatter/CI configured. `pom.xml` sets `maven.compiler.release=17` (build with JDK 17+; local JDK is 21).

## Design tokens live in `pom.xml`

All colors, radii, shadows, spacing, and gradients are `ui.*` Maven properties in `pom.xml`. The `maven-resources-plugin` runs **resource filtering over `**/*.css`**, substituting `${ui.*}` placeholders at build time, so:

- Source CSS in `src/main/resources/**/*.css` contains raw `${ui.color.primary}`-style placeholders (not valid CSS on their own).
- Change a color/spacing value in `pom.xml`, not in the CSS. The generated CSS in `target/` has the resolved values.
- Stylesheet load order is defined in `App.java` (`theme, animations, atoms, molecules, organisms, templates, pages, utilities`) — order matters for cascade.

## Data / services

- SQLite DB defaults to `novedadesale.db` in the process CWD. Override with `-Dpos.db=<path>` (or `-Dpos.db=:memory:`). In tests, use `BaseDatos.usarRuta(...)`.
- `BaseDatos` is a process-wide singleton; schema is created and `Seeder` runs only when the DB is empty. Seed users: `admin/admin123`, `caja1/caja123`.
- **Always access services through the `Servicios` locator** (`Servicios.productos()`, `Servicios.ventas().registrar(...)`, `Servicios.reportes()`, ...). Never instantiate a `*ServiceImpl` directly.
- Total/IVA math has a single source of truth: `CalculadoraTotales.calcular(items, descuentoPct)` (IVA 16%).
- Tests use temp SQLite files and call `BaseDatos.cerrar()` in `@AfterEach`/`@AfterAll`; keep that pattern for new tests.

## Conventions and constraints

- `models/`, `services/` interfaces, and `shared/` are treated as a **frozen contract**: prefer adding methods to the existing interfaces over creating parallel services. `docs/plan_trabajo_2_agentes.md` §6 lists the canonical signatures.
- Throw `ValidacionException` for bad input and `ServiceException` for operational failures; controllers surface them via `ui/Dialogos`.
- Passwords use PBKDF2-HMAC-SHA256 with per-user salt (`shared/Seguridad`), with legacy SHA-256 verification supported.
- `docs/plan_trabajo_2_agentes.md` tracks completed work (A0–A6 / B0–B8, más A7–A9 y B9–B11 de la Fase 2); pendientes de esa Fase 2: B12, PDF y A10/B13. `docs/reporte_estado_proyecto.md` is the current status summary. Check these before assuming a feature is unimplemented or deciding scope.
- Security hardening (RBAC fases 1 y 2) **is implemented**: fase 1 (2026-10-23) and fase 2 (2026-10-27) — sesión obligatoria, identidad de venta desde la sesión, cambio obligatorio de contraseña (`cambiarPasswordPropia`), bloqueo a los 5 intentos/15 min, política en `Validadores.passwordSegura`, cierre por inactividad (`ui/TemporizadorInactividad` + `shared/Reloj`). Informes: `docs/reporte_ejecucion_seguridad_rbac_fase1_2026-10-23.md` y `docs/reporte_ejecucion_seguridad_rbac_fase2_2026-10-27.md` (falta la verificación manual S13).
- `target/` and `*.db` are git-ignored; never commit database files.

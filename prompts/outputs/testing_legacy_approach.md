## Pruebas en aplicaciones Java legacy: una hoja de ruta pragmática

Establezcamos una hoja de ruta pragmática y de bajo riesgo para añadir tests unitarios a tu base de código Java legacy, con ayuda de IA y metas claras.

---

## Resumen del plan (fases, 3–7 pasos)

### 1) Fundamentos y herramientas (Días 1–3)

- Elige el stack principal:
	- `JUnit 5 (Jupiter)`, `Mockito` (incluye `mockito-inline` para clases finales), `AssertJ`.
	- `JaCoCo` para cobertura, `PIT` para testing de mutaciones.
	- Añade `WireMock` (HTTP), `Testcontainers` (BD/broker), `ArchUnit` (reglas de arquitectura) según necesidad.
- Integra en el build:
	- Maven: configura `maven-surefire-plugin`, `maven-failsafe-plugin` (para ITs), `jacoco-maven-plugin` y `pitest-maven`.
	- Gradle: `java-test-fixtures`, `jacoco`, plugin `pitest` si prefieres.
- Política de CI:
	- Ejecuta tests + cobertura en cada PR; publica reportes HTML; comienza con umbrales suaves (ej: cobertura de líneas ≥ 20–30% en archivos modificados).
- Convenciones de repo:
	- Estructura de tests: `src/test/java`, espeja la estructura de paquetes.
	- Nomenclatura: `ClassNameTest`, una intención de aserción por test con `@DisplayName`.
	- Determinismo: evita tiempo real, aleatoriedad, red; aísla usando fakes/mocks.

### 2) Dónde empezar (Días 3–5)

- Priorización basada en riesgo:
	- Áreas de alto cambio y alto impacto (por historial git, incidentes en producción).
	- Reglas de negocio núcleo con pocas dependencias externas.
- Tests de caracterización primero:
	- Escribe tests que codifiquen el comportamiento actual antes de refactorizar (enfoque Michael Feathers). Son tu red de seguridad.
- Métricas base:
	- Registra el conteo actual de tests, cobertura, score de mutaciones. Repite semanalmente.

### 3) Hacer el código testeable incrementalmente (Semana 2)

- Identifica “seams” y aplica refactors seguros que no cambian el comportamiento:
	- Inyección de dependencias para colaboradores (crea interfaces sobre llamadas estáticas).
	- Envuelve lo difícil: sistema de archivos, tiempo, aleatoriedad, variables de entorno, clientes HTTP.
	- Reemplaza `new` dentro de métodos por factories/proveedores.
	- Extrae funciones puras de métodos largos (sin IO/efectos secundarios).
	- Introduce límites para singletons estáticos (ej: `Clock`, `Random`, `FileOps`, adaptadores `HttpClient`).
- Protege con tests de caracterización, luego refactoriza, luego añade tests unitarios enfocados.

### 4) Haz crecer una suite de tests sostenible (Semanas 2–4)

- Tests unitarios para lógica de negocio:
	- AssertJ para aserciones expresivas, tests parametrizados para límites.
	- Tests basados en propiedades (ej: `jqwik`) para lógica pura con invariantes.
- Tests de integración donde los seams son costosos:
	- Usa `Testcontainers` para BD/cache/brokers; `WireMock` para stubs HTTP.
- Chequeos de contrato/arquitectura:
	- `ArchUnit` para reglas de capas y dependencias prohibidas.
	- `EqualsVerifier` para value objects.
- Testing de mutaciones:
	- Usa `PIT` en módulos clave semanalmente para detectar aserciones débiles; sube umbrales gradualmente.

### 5) Política y gobernanza (Semana 4+)

- Gates de PR (empieza suave, endurece):
	- Debe añadirse/ajustarse tests para código modificado.
	- Cobertura en líneas modificadas ≥ 60% (sube con el tiempo).
	- No nuevos tests inestables; limita duración de tests.
- Higiene de tests:
	- Una responsabilidad por test, claro Arrange/Act/Assert.
	- Determinismo: sin sleeps, sin red real, seeds estables para aleatoriedad.
	- Limpieza de recursos; prefiere `@TempDir` y try-with-resources.

### 6) Documentación y ciclo de aprendizaje

- Añade un `TESTING.md`:
	- Convenciones, ejemplos, patrones de fixtures, lista de do/don’t.
	- Cómo correr subconjuntos rápidos, cómo depurar fallos localmente.
- Métricas semanales:
	- Diferencia de cobertura en archivos modificados, score de mutaciones en paquetes clave, tasa de flakes.
- Itera:
	- Usa métricas y aprendizajes de incidentes para elegir próximos hotspots.

### 7) Escala con AI Copilot (continuo)

- Lo que la IA hace bien:
	- Generar esqueletos de tests con buenos nombres y bloques Arrange/Act/Assert.
	- Rellenar casos límite y entradas parametrizadas/de frontera.
	- Producir fakes/mocks rápido y sugerir refactors para añadir seams.
	- Resumir clases grandes y proponer escenarios de test.
- Guardarraíles:
	- Ejecuta siempre los tests generados; nunca confíes en código no ejecutado.
	- Revisa aserciones por corrección, evita sobre-mockear.
	- Prefiere aserciones legibles (AssertJ) y tests independientes.

---

## Casos frontera: qué son y cómo encontrarlos

Los casos frontera son entradas o condiciones en los “bordes” de los rangos válidos o interesantes donde es más probable que aparezcan bugs (off-by-one, overflow, estados vacíos).

- Categorías comunes:
	- Números: `0`, `1`, `-1`, min/máx del tipo (ej: `Integer.MIN_VALUE`, `Integer.MAX_VALUE`), justo debajo/encima de umbrales.
	- Colecciones: vacía (`size=0`), singleton (`size=1`), muy grande, elementos repetidos, desordenada vs ordenada.
	- Strings: vacía `""`, solo espacios, longitud máxima, Unicode/emoji, diferentes codificaciones, espacios iniciales/finales.
	- Optionals/nulos: parámetros `null`, campos faltantes, valores por defecto.
	- Tiempo: cambios de horario de verano, 29 de febrero, fin de mes, zonas horarias, epoch `0`.
	- Sistema de archivos/IO: rutas inexistentes, permisos denegados, disco lleno, nombres largos, caracteres especiales.
	- Config/env: variables de entorno faltantes, valores malformados, flags no soportados.
	- Concurrencia: escrituras simultáneas, timeouts, cancelaciones, reintentos al límite.
	- Aritmética: división por cero, overflow/underflow, límites de redondeo.

- Cómo identificarlos rápido:
	- Escanea condicionales y bucles: busca `>`, `>=`, `size()`, límites, paginación.
	- Revisa reglas de validación y constraints de DTOs.
	- Usa tests basados en propiedades para descubrir fronteras inesperadas.

---

## Cómo usar AI Copilot eficazmente

- Genera esqueletos de tests
	- “Escribe tests JUnit 5 para `ClassX.methodY`, incluye casos frontera para entradas A/B/C y rutas de error. Usa AssertJ y Mockito; evita IO real.”
- Crea mocks/fakes rápido
	- “Sugiere una abstracción `Clock` para esta clase y actualiza los tests para usar un tiempo fijo.”
- Deriva escenarios desde el código
	- “Resume las responsabilidades de `ClassX` y lista 10 escenarios de test cubriendo ramas y casos límite.”
- Código difícil de testear
	- “Propón un refactor para inyectar un seam de `HttpClient` en `ServiceX` y así poder simular respuestas con WireMock en los tests.”
- Tests basados en propiedades
	- “Genera tests jqwik para `Money.add` respetando asociatividad e identidad.”

Revisa siempre el código generado, ejecútalo y itera—la IA es un acelerador, no un sustituto de la corrección.

---

## Primeras 3 Pull Requests

1) Fundamento de testing
- Añade JUnit 5, Mockito, AssertJ, plugins JaCoCo; crea `TESTING.md` con convenciones.
- Añade un pequeño test “walking skeleton” para validar el setup.

2) Tests de caracterización para una clase crítica
- Elige una clase de alto valor; escribe 5–10 tests cubriendo el comportamiento actual (sin refactor aún).
- Añade reportes de cobertura y mutaciones a los artefactos de CI.

3) Refactor seguro para añadir un seam
- Introduce una interfaz alrededor de una dependencia difícil (tiempo, filesystem, HTTP).
- Reemplaza llamadas estáticas directas por la interfaz; actualiza tests para mockearla; añade tests unitarios enfocados.

---

## Obstáculos comunes en legacy y remedios rápidos

- Singletons estáticos / estado global → envuelve tras interfaces; añade factory/proveedor; pásalo por constructor.
- Instanciación directa de dependencias en métodos → usa factories o DI; evita efectos colaterales ocultos.
- Tiempo y aleatoriedad → `Clock` y `Random` con seed en tests.
- Llamadas a FS/red → abstracción `FileOperations`; `WireMock`/`Testcontainers`.
- Métodos grandes → extrae funciones puras; testéalas en aislamiento.

---

## Criterios de éxito

- Corto plazo: tests corren en CI, cobertura base en archivos modificados, una zona crítica cubierta con tests de caracterización.
- Medio plazo: crecimiento sostenido de cobertura y score de mutaciones en módulos clave; menos regresiones; revisiones más rápidas por patrones claros de test.
- Largo plazo: módulos críticos son testeables unitariamente; refactors son seguros y rápidos; CI exige alta calidad.

---

Si quieres, puedo:

- Añadir la config Maven y un `TESTING.md` mínimo a tu repo.
- Elegir una clase real de tu código, generar tests de caracterización iniciales y mostrar el refactor para añadir un seam + nuevos tests unitarios.
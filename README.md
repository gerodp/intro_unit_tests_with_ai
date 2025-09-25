# Demo Java Unit Testing con IA

Este proyecto es una demostración básica en Java para entender cómo implementar unit tests con IA

### Técnicas de Testing Utilizadas

1. **Mocking con Mockito**: Para aislar dependencias externas
2. **JUnit 5**: Framework de testing moderno con anotaciones expresivas
3. **Tests Parametrizados**: Para probar múltiples escenarios con una sola implementación
4. **Arrange-Act-Assert Pattern**: Estructura clara y legible de los tests
5. **Mock Verification**: Verificación de interacciones entre componentes

## 🚀 Ejecutar los Tests

### Todos los tests
```bash
mvn test
```

### Test específico
```bash
mvn test -Dtest=TestCache
```

### Con reporte detallado
```bash
mvn test -Dtest=TestCache -Dmaven.test.failure.ignore=true
```


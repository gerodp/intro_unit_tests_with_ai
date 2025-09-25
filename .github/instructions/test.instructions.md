---
applyTo: "src/test/**"
---

# GitHub Copilot Instructions for Unit Testing

## Project Context
This is a Java Maven project focusing on cache management with a legacy codebase that we're adding comprehensive unit tests to. The main classes include Cache, FileSystemCacheStore, CacheStore interface, and Retriever.

## Testing Framework & Dependencies
- **JUnit 5** (Jupiter) - Use modern JUnit 5 syntax and annotations
- **Mockito** - For mocking dependencies (include mockito-junit-jupiter for integration)
- **AssertJ** - For expressive assertions instead of basic JUnit assertions
- **Maven Surefire Plugin** - For test execution

## Code Style & Conventions

### Test Class Naming
- Test classes: `Test{ClassName}` (e.g., `TestCache`, `TestFileSystemCacheStore`)
- Place tests in `src/test/java` mirroring the package structure of `src/main/java`

### Test Method Naming & Structure
```java
@Test
@DisplayName("Should return cached data when cache hit occurs")
public void shouldReturnCachedDataWhenCacheHitOccurs() {
    // Arrange
    String url = "http://example.com/file.txt";
    byte[] expectedData = "test content".getBytes();
    
    // Act
    byte[] result = cache.get(url);
    
    // Assert
    assertThat(result).isEqualTo(expectedData);
}
```

### Mock Setup Pattern
```java
@ExtendWith(MockitoExtension.class)
public class CacheTest {
    @Mock
    private CacheStore<String, byte[]> mockCacheStore;
    
    @Mock
    private Retriever mockRetriever;
    
    @InjectMocks  // When possible, or manual construction in @BeforeEach
    private Cache cache;
    
    @BeforeEach
    void setUp() {
        // Additional setup if needed
        cache = new Cache(mockCacheStore, mockRetriever);
    }
}
```

## Testing Patterns & Best Practices

### 1. Arrange-Act-Assert Pattern
Always structure tests with clear sections:
- **Arrange**: Set up test data and mocks
- **Act**: Execute the method under test
- **Assert**: Verify results and interactions

### 2. Assertion Style
```java
// Use AssertJ for readable assertions
assertThat(result).isNotNull();
assertThat(result).hasSize(3);
assertThat(result).containsExactly("item1", "item2", "item3");

// For arrays (like byte[])
assertThat(retrievedData).isEqualTo(expectedData);

// For exceptions
assertThatThrownBy(() -> cache.get(null))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessage("URL cannot be null");
```

### 3. Mock Verification
```java
// Verify method calls
verify(mockCacheStore, times(1)).retrieve(url);
verify(mockRetriever, never()).retrieve(anyString());

// Verify with argument matchers
verify(mockCacheStore).store(eq(url), any(byte[].class));
```

### 4. Test Data & Scenarios
- Use meaningful test data: URLs, file content, etc.
- Cover happy path, edge cases, and error scenarios
- Test boundary conditions: null inputs, empty collections, max sizes
- Include negative test cases (expected failures)

### 5. Parameterized Tests for Multiple Scenarios
```java
@ParameterizedTest
@ValueSource(strings = {"", " ", "invalid-url", "http://"})
@DisplayName("Should handle invalid URLs appropriately")
void shouldHandleInvalidUrls(String invalidUrl) {
    // Test implementation
}
```

## Legacy Code Testing Approach

### 1. Characterization Tests First
When testing legacy code, start with characterization tests that document current behavior:
```java
@Test
@DisplayName("Documents current behavior of legacy method")
void documentsCurrentBehaviorOfLegacyMethod() {
    // Capture current behavior before refactoring
    String result = legacyService.process("input");
    assertThat(result).isEqualTo("expected current output");
}
```

### 2. Seam Identification for Testability
- Extract interfaces for hard dependencies (file system, time, network)
- Use dependency injection to enable mocking
- Create wrapper classes for static calls

### 3. Mock External Dependencies
```java
// Instead of real file operations
@Mock
private FileOperations mockFileOps;

// Instead of real time
@Mock
private Clock mockClock;
```

## TDD Workflow
When doing Test-Driven Development:

1. **Red**: Write failing test first
2. **Green**: Write minimal code to pass
3. **Refactor**: Improve code while keeping tests green

```java
// Step 1: Write test that fails compilation
@Test
void shouldCleanCache() {
    cache.clean(); // Method doesn't exist yet
    verify(mockCacheStore).clear();
}

// Step 2: Add minimal implementation
public void clean() {
    mCacheStore.clear();
}
```

## Error Scenarios & Edge Cases to Test

### Common Edge Cases
- Null inputs
- Empty collections/strings
- Boundary values (0, 1, -1, MAX_VALUE, MIN_VALUE)
- Very large inputs
- Concurrent access scenarios
- Network/IO failures (simulated)

### Exception Testing
```java
@Test
void shouldThrowExceptionWhenUrlIsNull() {
    assertThatThrownBy(() -> cache.get(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("URL cannot be null");
}
```


## Integration vs Unit Tests
- **Unit tests**: Mock all dependencies, test in isolation
- **Integration tests**: Use @TestContainer for databases, WireMock for HTTP services
- **End-to-end tests**: Minimal, test critical user journeys

## Performance Considerations
- Keep unit tests fast (<100ms each)
- Use @TempDir instead of real file operations when possible
- Mock time-dependent code with Clock abstraction
- Avoid Thread.sleep() - use deterministic test doubles

## Code Coverage Goals
- Aim for >80% line coverage on new code
- Focus on branch coverage for complex logic
- Use mutation testing (PIT) to verify test quality
- Don't sacrifice test readability for coverage percentage

## Anti-Patterns to Avoid
- Testing implementation details instead of behavior
- Over-mocking (mocking value objects or simple data structures)
- Brittle tests that break on refactoring
- Tests that depend on execution order
- Using real external services in unit tests
- Ignoring test failures ("flaky tests")

## When to Use Different Test Types
- **Unit tests**: Business logic, algorithms, validation
- **Integration tests**: Database interactions, message queues
- **Contract tests**: API interfaces, external service interactions
- **Property-based tests**: Mathematical operations, data transformations
package com.example.cache;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;


public class TestCache {

    @Mock
    private CacheStore<String, byte[]> mockStorage;

    @Mock
    private Retriever mockRetriever;
    
    private Cache cache;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        cache = new Cache(mockStorage, mockRetriever);
    }

    @Test
    public void shouldReturnFileFromLocalWhenPresentInCache() {
        String fileUrl = "https://google.com/logo.jpg";
        byte[] fileContent = "LOGO".getBytes();

        when(mockStorage.retrieve(fileUrl)).thenReturn(fileContent);

        byte[] data = cache.get(fileUrl);

        assertEquals(fileContent, data);

        verify(mockRetriever, never()).retrieve(anyString());
    }

    @Test
    public void shouldRetrieveStoreAndReturnFileWhenNotPresentInCache() {
        String fileUrl = "http://google.com/logo.jpg";
        byte[] fileContent = "CONTENIDO FILE 1".getBytes();

        when(mockStorage.retrieve(fileUrl)).thenReturn(null);
        when(mockRetriever.retrieve(fileUrl)).thenReturn(fileContent);

        byte[] data = cache.get(fileUrl);

        assertEquals(fileContent, data);

        verify(mockStorage, times(1)).retrieve(fileUrl);
        verify(mockRetriever, times(1)).retrieve(fileUrl);
        verify(mockStorage, times(1)).store(fileUrl, fileContent);
    }

    @Test
    @DisplayName("Should handle null URL appropriately")
    public void shouldHandleNullUrl() {
        // Arrange
        String nullUrl = null;

            byte[] result = cache.get(nullUrl);
            // If we get here, the method returned without exception
            assertNull(result, "Cache should return null for null URL");
            
            // Verify that neither storage nor retriever were called with null
            verify(mockStorage, never()).retrieve(null);
            verify(mockRetriever, never()).retrieve(null);

    }

    @Test
    @DisplayName("Should handle empty URL appropriately")
    public void shouldHandleEmptyUrl() {
        // Arrange
        String emptyUrl = "";

        // Act
        byte[] result = cache.get(emptyUrl);

        // Assert
        assertNull(result, "Cache should return null for empty URL");
        
        // Verify interactions
        verify(mockStorage, never()).retrieve(emptyUrl);
        verify(mockRetriever, never()).retrieve(emptyUrl);
        verify(mockStorage, never()).store(anyString(), any(byte[].class));
    }

}

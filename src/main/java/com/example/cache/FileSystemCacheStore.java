
package com.example.cache;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

public class FileSystemCacheStore implements CacheStore<String, byte[]> {
    
    private final String cacheDirectory;
    
    public FileSystemCacheStore(String cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
        createCacheDirectoryIfNotExists();
    }
    
    public FileSystemCacheStore() {
        this(System.getProperty("user.home") + File.separator + ".cache" + File.separator + "file-cache");
    }
    
    private void createCacheDirectoryIfNotExists() {
        try {
            Path cachePath = Paths.get(cacheDirectory);
            if (!Files.exists(cachePath)) {
                Files.createDirectories(cachePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create cache directory: " + cacheDirectory, e);
        }
    }
    
    @Override
    public boolean store(String url, byte[] data) {
        try {
            String fileName = urlToFileName(url);
            Path filePath = Paths.get(cacheDirectory, fileName);
            Files.write(filePath, data);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to store file for URL: " + url + ". Error: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public byte[] retrieve(String url) {
        try {
            String fileName = urlToFileName(url);
            Path filePath = Paths.get(cacheDirectory, fileName);
            if (Files.exists(filePath)) {
                return Files.readAllBytes(filePath);
            }
            return null;
        } catch (IOException e) {
            System.err.println("Failed to retrieve file for URL: " + url + ". Error: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean remove(String url) {
        try {
            String fileName = urlToFileName(url);
            Path filePath = Paths.get(cacheDirectory, fileName);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to remove file for URL: " + url + ". Error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Converts a URL to a safe filename by creating a hash
     * This avoids issues with special characters in URLs
     */
    private String urlToFileName(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(url.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString() + ".cache";
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash if SHA-256 is not available
            return String.valueOf(url.hashCode()) + ".cache";
        }
    }
    
    /**
     * Gets the cache directory path
     */
    public String getCacheDirectory() {
        return cacheDirectory;
    }

    public void clear() {
        try (Stream<Path> files = Files.walk(Paths.get(cacheDirectory))) {
            files.filter(Files::isRegularFile)
                 .forEach(file -> {
                     try {
                         Files.delete(file);
                     } catch (IOException e) {
                         System.err.println("Failed to delete file: " + file.toString() + ". Error: " + e.getMessage());
                     }
                 });
        } catch (IOException e) {
            System.err.println("Failed to clear cache directory: " + cacheDirectory + ". Error: " + e.getMessage());
        }
    }
}

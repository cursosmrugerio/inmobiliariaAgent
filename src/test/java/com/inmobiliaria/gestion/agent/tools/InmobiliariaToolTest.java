package com.inmobiliaria.gestion.agent.tools;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.inmobiliaria.gestion.exception.ResourceNotFoundException;
import com.inmobiliaria.gestion.inmobiliaria.dto.CreateInmobiliariaRequest;
import com.inmobiliaria.gestion.inmobiliaria.dto.InmobiliariaResponse;
import com.inmobiliaria.gestion.inmobiliaria.dto.UpdateInmobiliariaRequest;
import com.inmobiliaria.gestion.inmobiliaria.service.InmobiliariaService;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InmobiliariaToolTest {

  @Mock private InmobiliariaService inmobiliariaService;

  @InjectMocks private InmobiliariaTool inmobiliariaTool;

  private InmobiliariaResponse sampleResponse;

  @BeforeEach
  void setUp() {
    sampleResponse =
        new InmobiliariaResponse(
            1L,
            "Inmobiliaria Test",
            "ABC123456789",
            "Test Contact",
            "test@example.com",
            "+52-55-1234-5678");
  }

  @Test
  void listAllInmobiliarias_Success() {
    // Given
    List<InmobiliariaResponse> responses = Arrays.asList(sampleResponse);
    when(inmobiliariaService.findAll()).thenReturn(responses);

    // When
    Map<String, Object> result = inmobiliariaTool.listAllInmobiliarias();

    // Then
    assertTrue((Boolean) result.get("success"));
    assertEquals(1, result.get("count"));
    assertNotNull(result.get("inmobiliarias"));
    verify(inmobiliariaService, times(1)).findAll();
  }

  @Test
  void listAllInmobiliarias_Error() {
    // Given
    when(inmobiliariaService.findAll()).thenThrow(new RuntimeException("Database error"));

    // When
    Map<String, Object> result = inmobiliariaTool.listAllInmobiliarias();

    // Then
    assertFalse((Boolean) result.get("success"));
    assertNotNull(result.get("error"));
    assertTrue(((String) result.get("error")).contains("Error listing inmobiliarias"));
  }

  @Test
  void getInmobiliariaById_Success() {
    // Given
    when(inmobiliariaService.findById(1L)).thenReturn(sampleResponse);

    // When
    Map<String, Object> result = inmobiliariaTool.getInmobiliariaById(1);

    // Then
    assertTrue((Boolean) result.get("success"));
    assertNotNull(result.get("inmobiliaria"));
    verify(inmobiliariaService, times(1)).findById(1L);
  }

  @Test
  void getInmobiliariaById_NotFound() {
    // Given
    when(inmobiliariaService.findById(999L)).thenThrow(new ResourceNotFoundException("Not found"));

    // When
    Map<String, Object> result = inmobiliariaTool.getInmobiliariaById(999);

    // Then
    assertFalse((Boolean) result.get("success"));
    assertNotNull(result.get("error"));
  }

  @Test
  void createInmobiliaria_Success() {
    // Given
    when(inmobiliariaService.create(any(CreateInmobiliariaRequest.class)))
        .thenReturn(sampleResponse);

    // When
    Map<String, Object> result =
        inmobiliariaTool.createInmobiliaria(
            "Inmobiliaria Test",
            "ABC123456789",
            "Test Contact",
            "test@example.com",
            "+52-55-1234-5678");

    // Then
    assertTrue((Boolean) result.get("success"));
    assertNotNull(result.get("inmobiliaria"));
    assertEquals("Inmobiliaria created successfully", result.get("message"));
    verify(inmobiliariaService, times(1)).create(any(CreateInmobiliariaRequest.class));
  }

  @Test
  void createInmobiliaria_Error() {
    // Given
    when(inmobiliariaService.create(any(CreateInmobiliariaRequest.class)))
        .thenThrow(new RuntimeException("Validation error"));

    // When
    Map<String, Object> result =
        inmobiliariaTool.createInmobiliaria("Test", null, null, null, null);

    // Then
    assertFalse((Boolean) result.get("success"));
    assertNotNull(result.get("error"));
  }

  @Test
  void updateInmobiliaria_Success() {
    // Given
    when(inmobiliariaService.update(eq(1L), any(UpdateInmobiliariaRequest.class)))
        .thenReturn(sampleResponse);

    // When
    Map<String, Object> result =
        inmobiliariaTool.updateInmobiliaria(
            1, "Updated Name", "NEW123456789", "New Contact", "new@example.com", "+52-55-9999");

    // Then
    assertTrue((Boolean) result.get("success"));
    assertNotNull(result.get("inmobiliaria"));
    assertEquals("Inmobiliaria updated successfully", result.get("message"));
    verify(inmobiliariaService, times(1)).update(eq(1L), any(UpdateInmobiliariaRequest.class));
  }

  @Test
  void updateInmobiliaria_NotFound() {
    // Given
    when(inmobiliariaService.update(eq(999L), any(UpdateInmobiliariaRequest.class)))
        .thenThrow(new ResourceNotFoundException("Not found"));

    // When
    Map<String, Object> result =
        inmobiliariaTool.updateInmobiliaria(999, "Test", null, null, null, null);

    // Then
    assertFalse((Boolean) result.get("success"));
    assertNotNull(result.get("error"));
  }

  @Test
  void deleteInmobiliaria_Success() {
    // Given
    doNothing().when(inmobiliariaService).delete(1L);

    // When
    Map<String, Object> result = inmobiliariaTool.deleteInmobiliaria(1);

    // Then
    assertTrue((Boolean) result.get("success"));
    assertTrue(((String) result.get("message")).contains("deleted successfully"));
    verify(inmobiliariaService, times(1)).delete(1L);
  }

  @Test
  void deleteInmobiliaria_NotFound() {
    // Given
    doThrow(new ResourceNotFoundException("Not found")).when(inmobiliariaService).delete(999L);

    // When
    Map<String, Object> result = inmobiliariaTool.deleteInmobiliaria(999);

    // Then
    assertFalse((Boolean) result.get("success"));
    assertNotNull(result.get("error"));
  }
}

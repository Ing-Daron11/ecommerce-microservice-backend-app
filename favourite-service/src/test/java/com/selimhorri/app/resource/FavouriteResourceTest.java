package com.selimhorri.app.resource;

import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.service.FavouriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavouriteResourceTest {
    @Mock
    private FavouriteService favouriteService;

    @InjectMocks
    private FavouriteResource favouriteResource;

    private FavouriteDto testFavouriteDto;

    @BeforeEach
    void setUp() {
        testFavouriteDto = FavouriteDto.builder()
                .userId(1)
                .productId(100)
                .build();
    }

    @Test
    void findAllShouldReturnFavourites() {
        List<FavouriteDto> favourites = new ArrayList<>();
        favourites.add(testFavouriteDto);
        when(favouriteService.findAll()).thenReturn(favourites);

        ResponseEntity<?> response = favouriteResource.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(favouriteService, times(1)).findAll();
    }

    @Test
    void findByIdShouldReturnFavourite() {
        when(favouriteService.findById(any(FavouriteId.class))).thenReturn(testFavouriteDto);

        ResponseEntity<?> response = favouriteResource.findById("1", "100");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(favouriteService, times(1)).findById(any(FavouriteId.class));
    }

    @Test
    void saveShouldCreateFavourite() {
        when(favouriteService.save(any(FavouriteDto.class))).thenReturn(testFavouriteDto);

        ResponseEntity<?> response = favouriteResource.save(testFavouriteDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(favouriteService, times(1)).save(any(FavouriteDto.class));
    }

    @Test
    void deleteByIdShouldDeleteFavourite() {
        doNothing().when(favouriteService).deleteById(any(FavouriteId.class));

        ResponseEntity<?> response = favouriteResource.deleteById("1", "100");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(favouriteService, times(1)).deleteById(any(FavouriteId.class));
    }
}

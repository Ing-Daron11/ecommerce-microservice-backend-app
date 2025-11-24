package com.selimhorri.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.domain.Favourite;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.DuplicateEntityException;
import com.selimhorri.app.exception.wrapper.FavouriteNotFoundException;
import com.selimhorri.app.exception.wrapper.ProductNotFoundException;
import com.selimhorri.app.exception.wrapper.UserNotFoundException;
import com.selimhorri.app.repository.FavouriteRepository;

@ExtendWith(MockitoExtension.class)
class FavouriteServiceImplTest {

    @Mock
    private FavouriteRepository favouriteRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FavouriteServiceImpl favouriteService;

    private Favourite favourite;
    private FavouriteDto favouriteDto;
    private FavouriteId favouriteId;
    private UserDto userDto;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        favouriteId = new FavouriteId(1, 100, LocalDateTime.now());
        
        favourite = new Favourite();
        favourite.setUserId(1);
        favourite.setProductId(100);
        favourite.setLikeDate(favouriteId.getLikeDate());
        
        favouriteDto = new FavouriteDto();
        favouriteDto.setUserId(1);
        favouriteDto.setProductId(100);
        favouriteDto.setLikeDate(favouriteId.getLikeDate());

        userDto = new UserDto();
        userDto.setUserId(1);

        productDto = new ProductDto();
        productDto.setProductId(100);
    }

    @Test
    void shouldFindAll() {
        given(favouriteRepository.findAll()).willReturn(Collections.singletonList(favourite));
        given(restTemplate.getForObject(anyString(), eq(UserDto.class))).willReturn(userDto);
        given(restTemplate.getForObject(anyString(), eq(ProductDto.class))).willReturn(productDto);

        List<FavouriteDto> result = favouriteService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1);
        verify(favouriteRepository, times(1)).findAll();
    }

    @Test
    void shouldFindById() {
        given(favouriteRepository.findByUserIdAndProductId(anyInt(), anyInt())).willReturn(Optional.of(favourite));
        given(restTemplate.getForObject(anyString(), eq(UserDto.class))).willReturn(userDto);
        given(restTemplate.getForObject(anyString(), eq(ProductDto.class))).willReturn(productDto);

        FavouriteDto result = favouriteService.findById(favouriteId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1);
        verify(favouriteRepository, times(1)).findByUserIdAndProductId(anyInt(), anyInt());
    }

    @Test
    void shouldThrowExceptionWhenFavouriteNotFoundById() {
        given(favouriteRepository.findByUserIdAndProductId(anyInt(), anyInt())).willReturn(Optional.empty());

        assertThatThrownBy(() -> favouriteService.findById(favouriteId))
                .isInstanceOf(FavouriteNotFoundException.class)
                .hasMessageContaining("not found in database");
    }

    @Test
    void shouldSaveFavourite() {
        given(restTemplate.getForEntity(anyString(), eq(UserDto.class)))
                .willReturn(new ResponseEntity<>(userDto, HttpStatus.OK));
        given(restTemplate.getForEntity(anyString(), eq(ProductDto.class)))
                .willReturn(new ResponseEntity<>(productDto, HttpStatus.OK));
        given(favouriteRepository.existsByUserIdAndProductId(anyInt(), anyInt())).willReturn(false);
        given(favouriteRepository.save(any(Favourite.class))).willReturn(favourite);

        FavouriteDto result = favouriteService.save(favouriteDto);

        assertThat(result).isNotNull();
        verify(favouriteRepository, times(1)).save(any(Favourite.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingWithNonExistentUser() {
        given(restTemplate.getForEntity(anyString(), eq(UserDto.class)))
                .willReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> favouriteService.save(favouriteDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with id [1] not found");
        
        verify(favouriteRepository, never()).save(any(Favourite.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingWithNonExistentProduct() {
        given(restTemplate.getForEntity(anyString(), eq(UserDto.class)))
                .willReturn(new ResponseEntity<>(userDto, HttpStatus.OK));
        given(restTemplate.getForEntity(anyString(), eq(ProductDto.class)))
                .willReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> favouriteService.save(favouriteDto))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Product with id [100] not found");
        
        verify(favouriteRepository, never()).save(any(Favourite.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingDuplicateFavourite() {
        given(restTemplate.getForEntity(anyString(), eq(UserDto.class)))
                .willReturn(new ResponseEntity<>(userDto, HttpStatus.OK));
        given(restTemplate.getForEntity(anyString(), eq(ProductDto.class)))
                .willReturn(new ResponseEntity<>(productDto, HttpStatus.OK));
        given(favouriteRepository.existsByUserIdAndProductId(anyInt(), anyInt())).willReturn(true);

        assertThatThrownBy(() -> favouriteService.save(favouriteDto))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("Favourite already exists");
        
        verify(favouriteRepository, never()).save(any(Favourite.class));
    }

    @Test
    void shouldDeleteById() {
        given(favouriteRepository.existsByUserIdAndProductId(anyInt(), anyInt())).willReturn(true);

        favouriteService.deleteById(favouriteId);

        verify(favouriteRepository, times(1)).deleteByUserIdAndProductId(anyInt(), anyInt());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentFavourite() {
        given(favouriteRepository.existsByUserIdAndProductId(anyInt(), anyInt())).willReturn(false);

        assertThatThrownBy(() -> favouriteService.deleteById(favouriteId))
                .isInstanceOf(FavouriteNotFoundException.class)
                .hasMessageContaining("Favourite not found");
        
        verify(favouriteRepository, never()).deleteByUserIdAndProductId(anyInt(), anyInt());
    }
}

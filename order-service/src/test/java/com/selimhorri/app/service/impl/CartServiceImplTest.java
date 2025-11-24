package com.selimhorri.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.CartNotFoundException;
import com.selimhorri.app.exception.wrapper.UserNotFoundException;
import com.selimhorri.app.repository.CartRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart cart;
    private CartDto cartDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setCartId(1);
        cart.setUserId(100);
        cart.setActive(true);

        userDto = new UserDto();
        userDto.setUserId(100);
        userDto.setFirstName("John");

        cartDto = new CartDto();
        cartDto.setCartId(1);
        cartDto.setUserId(100);
        cartDto.setUserDto(userDto);
    }

    @Test
    void shouldFindAll() {
        given(cartRepository.findAllByIsActiveTrue()).willReturn(List.of(cart));
        given(restTemplate.getForObject(anyString(), eq(UserDto.class))).willReturn(userDto);

        List<CartDto> result = cartService.findAll();

        assertThat(result).hasSize(1);
        verify(cartRepository, times(1)).findAllByIsActiveTrue();
        verify(restTemplate, times(1)).getForObject(anyString(), eq(UserDto.class));
    }

    @Test
    void shouldFindById() {
        given(cartRepository.findByCartIdAndIsActiveTrue(1)).willReturn(Optional.of(cart));
        given(restTemplate.getForObject(anyString(), eq(UserDto.class))).willReturn(userDto);

        CartDto result = cartService.findById(1);

        assertThat(result).isNotNull();
        assertThat(result.getCartId()).isEqualTo(1);
        verify(cartRepository, times(1)).findByCartIdAndIsActiveTrue(1);
    }

    @Test
    void shouldThrowExceptionWhenCartNotFoundById() {
        given(cartRepository.findByCartIdAndIsActiveTrue(1)).willReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.findById(1))
                .isInstanceOf(CartNotFoundException.class)
                .hasMessageContaining("Active cart with id: 1 not found");
    }

    @Test
    void shouldSaveCart() {
        given(restTemplate.getForObject(anyString(), eq(UserDto.class))).willReturn(userDto);
        given(cartRepository.save(any(Cart.class))).willReturn(cart);

        CartDto result = cartService.save(cartDto);

        assertThat(result).isNotNull();
        verify(restTemplate, times(1)).getForObject(anyString(), eq(UserDto.class));
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingCartWithoutUserId() {
        cartDto.setUserId(null);

        assertThatThrownBy(() -> cartService.save(cartDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserId must not be null");
        
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringSave() {
        given(restTemplate.getForObject(anyString(), eq(UserDto.class))).willThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> cartService.save(cartDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with id 100 not found");
        
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void shouldDeleteById() {
        given(cartRepository.findById(1)).willReturn(Optional.of(cart));

        cartService.deleteById(1);

        verify(cartRepository, times(1)).save(cart);
        assertThat(cart.isActive()).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentCart() {
        given(cartRepository.findById(1)).willReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.deleteById(1))
                .isInstanceOf(CartNotFoundException.class)
                .hasMessageContaining("Cart with id: 1 not found");
        
        verify(cartRepository, never()).save(any(Cart.class));
    }
}

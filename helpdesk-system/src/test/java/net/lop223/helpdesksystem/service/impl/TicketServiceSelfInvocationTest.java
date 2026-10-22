package net.lop223.helpdesksystem.service.impl;

import net.lop223.helpdesksystem.dto.response.TicketResponse;
import net.lop223.helpdesksystem.entity.Ticket;
import net.lop223.helpdesksystem.mapper.TicketMapper;
import net.lop223.helpdesksystem.repository.AppUserRepository;
import net.lop223.helpdesksystem.repository.CategoryRepository;
import net.lop223.helpdesksystem.repository.TicketRepository;
import net.lop223.helpdesksystem.service.TicketService;
import net.lop223.helpdesksystem.service.async.TicketAsyncCoordinatorService;
import net.lop223.helpdesksystem.utils.aspect.MaskSensitiveDataAspect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Жодного @SpringBootTest!
class TicketServiceSelfInvocationUnitTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private TicketMapper ticketMapper;
    @Mock
    private TicketAsyncCoordinatorService ticketAsyncCoordinatorService;

    // Це буде наш Справжній Spring Proxy, але створений без контексту
    private TicketService ticketServiceProxy;

    private final String RAW_DESCRIPTION = "Secret card: 1111-2222-3333-4444";

    @BeforeEach
    void setUp() {
        // 1. Створюємо реальний екземпляр сервісу зі звичайними моками через конструктор
        TicketServiceImpl targetService = new TicketServiceImpl(
                ticketRepository, categoryRepository, appUserRepository, ticketMapper, ticketAsyncCoordinatorService
        );

        // 2. Створюємо Spring Proxy засобами AspectJProxyFactory
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(targetService);

        // Додаємо наш аспект у фабрику проксі
        proxyFactory.addAspect(new MaskSensitiveDataAspect());

        // Отримуємо проксі-об'єкт (той самий, який Spring створює в рантаймі!)
        this.ticketServiceProxy = proxyFactory.getProxy();

        // 3. Базові моки для повернення тікета
        Ticket mockTicket = new Ticket();
        mockTicket.setId(1L);
        mockTicket.setDescription(RAW_DESCRIPTION);

        TicketResponse mockResponse = new TicketResponse();
        mockResponse.setId(1L);
        mockResponse.setDescription(RAW_DESCRIPTION);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(mockTicket));
        when(ticketMapper.toResponse(any())).thenReturn(mockResponse);
    }

    @Test
    @DisplayName("Сценарій 1: Зовнішній виклик іде через Проксі -> Аспект СПРАЦЬОВУЄ")
    void externalInvocation_shouldTriggerAspectAndMaskData() {
        // WHEN: викликаємо метод напряму на проксі-об'єкті
        TicketResponse result = ticketServiceProxy.getById(1L);

        // THEN: аспект перехопив виклик і замаскував дані
        assertNotNull(result);
        assertNotEquals(RAW_DESCRIPTION, result.getDescription());
        assertTrue(result.getDescription().contains("********"),
                "Опис повинен бути замаскований аспектом");
    }

    @Test
    @DisplayName("Сценарій 2: Самовиклик (this.getById()) оминає Проксі -> Аспект НЕ СПРАЦЬОВУЄ")
    void selfInvocation_shouldBypassProxyAndNOTTriggerAspect() {
        // WHEN: викликаємо getTicketsBatch, який всередині робить this.getById(1L)
        List<TicketResponse> batch = ticketServiceProxy.getTicketsBatch(List.of(1L));

        // THEN: перевіряємо, що через self-invocation аспект НЕ спрацював
        assertNotNull(batch);
        assertEquals(1, batch.size());

        TicketResponse result = batch.get(0);

        // ГОЛОВНА ПЕРЕВІРКА: дані залишилися сирими (НЕ замаскованими)
        assertEquals(RAW_DESCRIPTION, result.getDescription(),
                "Через self-invocation проксі оминається, тому опис НЕ повинен змінитися");
        assertFalse(result.getDescription().contains("[CONFIDENTIAL DATA MASKED]"));
    }
}
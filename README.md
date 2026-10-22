# Лабораторна робота №5 — Асинхронна обробка

## Мета

Мета роботи — винести повільні побічні операції зі звичайного потоку обробки заявки, щоб вони не затримували її створення. Для цього налаштовано окремий пул потоків і додано обробку помилок, тайм-аутів та перевантаження.


## Що було змінено


У `AsyncConfig` увімкнено асинхронну обробку та налаштовано `ThreadPoolTaskExecutor`: основний пул — 3 потоки, максимальний — 6, черга — 20 завдань. Префікс `ticket-async-` дає змогу впізнати ці потоки в логах. Для невідловлених винятків у методах `@Async void` налаштовано `AsyncUncaughtExceptionHandler`.


Java






```
executor.setCorePoolSize(3);
executor.setMaxPoolSize(6);
executor.setQueueCapacity(20);
executor.setThreadNamePrefix("ticket-async-");

```





У `TicketAsyncTasksService` реалізовано три фонові операції: імітацію надсилання email, створення PDF-квитанції та надсилання webhook. Методи позначені `@Async("ticketExecutor")`, а PDF і webhook повертають `CompletableFuture<String>`.


Java






```
@Async("ticketExecutor")
public CompletableFuture<String> generatePdfReceipt(Long ticketId) {
    simulateSlowExternalCall();
    return CompletableFuture.completedFuture(
            "Generated PDF receipt for ticketId: " + ticketId);
}

```





Затримка в методах імітує повільний зовнішній виклик. Вона потрібна для демонстрації асинхронності; у реальному застосунку її слід замінити на інтеграцію з поштовим сервісом, генератором PDF чи зовнішнім API.


`TicketAsyncCoordinatorService` запускає незалежні задачі та об’єднує їхні результати через `thenCombine`. Метод `orTimeout` обмежує очікування п’ятьма секундами, а `handle` перетворює виняток або тайм-аут на невдалий результат. Якщо executor відхиляє завдання, координатор повертає повідомлення про зайнятість, а не перериває всю обробку.


Java






```
return pdf.thenCombine(webhook,
        (pdfResult, webhookResult) ->
                new TicketAsyncSummary(ticketResponse.getId(), pdfResult, webhookResult));

```





Координатор використовується в `TicketServiceImpl.create()`: після збереження заявки сервіс запускає фонові операції через `dispatchAfterCreation(...)`, реєструє обробник завершення через `whenComplete(...)` і повертає відповідь із заявкою, не чекаючи завершення PDF чи webhook.


## Тести



- **`TicketAsyncCoordinatorServiceTest`** — перевіряє запуск і об’єднання двох задач, обробку помилки Future, тайм-ауту та відхилення задач executor-ом, а також продовження роботи, якщо не вдалося запустити email-задачу.

- **`AsyncInfrastructureTest`** — перевіряє параметри пулу, префікс потоків, використання `ticketExecutor`, паралельне виконання задач і наявність обробника винятків для асинхронних `void`-методів.

- **`TicketAsyncSaturationTest`** — перевіряє поведінку координатора, коли малий пул і черга переповнюються та executor відхиляє одну із задач.

- **`TicketServiceAsyncDispatchTest`** — перевіряє, що створення заявки повертає відповідь і запускає фонову обробку, не очікуючи завершення Future.



## Висновок


Асинхронні операції виконуються у власному обмеженому пулі потоків, незалежно від потоку, який створює заявку. `CompletableFuture` дає змогу об’єднувати результати, обробляти помилки й обмежувати очікування тайм-аутом. Переповнення executor-а також обробляється контрольовано.
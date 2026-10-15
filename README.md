# Лабораторна робота №4 — AOP

## Мета

У роботі застосовано Aspect-Oriented Programming (AOP) для винесення наскрізної поведінки з бізнес-методів сервісів: маскування чутливих даних у відповіді, аудиту успішних дій і захисту системних категорій від видалення. Поведінка вмикається анотаціями на конкретних методах.

## Як це реалізовано

Кожна анотація позначена `@Target(ElementType.METHOD)`, тому її можна застосувати до методу, і `@Retention(RetentionPolicy.RUNTIME)`, щоб Spring AOP міг знайти її під час виконання. Класи аспектів позначені `@Aspect` та `@Component`, отже Spring реєструє їх як компоненти й підключає до відповідних викликів через pointcut `@annotation(...)`.

У проєкті є три зв'язки «анотація — аспект». Кожна має власну точку та час виконання: **`@Before` — до цільового методу, `@AfterReturning` — після його успішного завершення, `@Around` — навколо його виконання**.

| Анотація та аспект | Advice | Призначення |
|---|---|---|
| `@ProtectSystemEntity` — `ProtectSystemEntityAspect` | `@Before` | Перевірити аргумент-ідентифікатор до виконання цільового методу та заборонити зміну захищеної сутності. |
| `@AuditAction` — `AuditActionAspect` | `@AfterReturning` | Записати дію в аудит лише після успішного завершення цільового методу. |
| `@MaskSensitiveData` — `MaskSensitiveDataAspect` | `@Around` | Виконати метод, обробити його результат і повернути його з замаскованим описом. |

### `@ProtectSystemEntity` і `ProtectSystemEntityAspect`

Анотація містить параметри `reservedIds` та `message`. За замовчуванням захищений ID — `1`, а повідомлення — `System entity cannot be modified`. В аспекті `@Before` означає, що перевірка виконується **до** бізнес-методу. Аспект шукає серед аргументів методу значення типу `Long`; якщо воно входить до `reservedIds`, кидає `OperationNotAllowedException`. У такому разі видалення не виконується.

Наприклад, у `CategoryServiceImpl`:

```java
@PreAuthorize("hasRole('ADMIN')")
@ProtectSystemEntity(reservedIds = {1L, 3L})
public void delete(Long id) {
    Category category = findEntityById(id);
    categoryRepository.delete(category);
}
```

Виклик `delete(1L)` або `delete(3L)` буде відхилено до пошуку та видалення категорії. Інші ID пройдуть перевірку, після чого метод продовжить звичайне виконання.

### `@AuditAction` і `AuditActionAspect`

Анотація приймає обов'язковий параметр `action` — назву дії для журналу аудиту. Аспект використовує `@AfterReturning`, тому `auditLogService.logAction(...)` викликається **лише після успішного повернення** цільового методу. Якщо метод завершився винятком, цей advice не виконується.

Для `AppUserResponse` ідентифікатор береться з `result.getId()`. Якщо результат не є `AppUserResponse` (наприклад, метод `delete` повертає `void`), аспект шукає перший аргумент типу `Long`. До журналу передаються назва дії, ID користувача та назва методу.

Приклади з `AppUserServiceImpl`:

```java
@AuditAction(action = "USER_CREATED")
public AppUserResponse create(AppUserRequest request) {
    // створення та збереження користувача
}

@AuditAction(action = "USER_DELETED")
public void delete(Long id) {
    // пошук та видалення користувача
}
```

Після успішного `create` ID визначається з поверненого `AppUserResponse`; після успішного `delete` — з аргументу `id`. `AuditLogServiceImpl` записує подію через SLF4J у форматі `[AUDIT TRAIL]`.

### `@MaskSensitiveData` і `MaskSensitiveDataAspect`

Анотація дає змогу налаштувати рядок заміни через `mask` (за замовчуванням `********`) і текстовий префікс `maskDescription` (за замовчуванням `Masked`). Аспект використовує `@Around`: спочатку викликає початковий метод через `joinPoint.proceed()`, а потім обробляє результат. Якщо результат є `TicketResponse` і його опис не `null`, регулярний вираз замінює в описі email-адреси та рядки, схожі на 16-значні номери карток. Змінений `TicketResponse` повертається виклику.

Приклад застосування в `TicketServiceImpl`:

```java
@MaskSensitiveData
public TicketResponse getById(Long id) {
    return ticketMapper.toResponse(findEntityById(id));
}
```

Якщо опис містить `user@example.com`, він буде замінений на `********`. Якщо регулярний вираз нічого не знайшов, поточна реалізація додає до опису префікс `Masked `. Результати інших типів аспект не змінює.

## Обмеження проксі та self-invocation

Spring AOP перехоплює виклики, що проходять через Spring-проксі. Якщо метод того самого класу викликається напряму через `this`, виклик обходить проксі, тому відповідний advice не запускається.

У `TicketServiceImpl.getTicketsBatch` є внутрішній виклик `this.getById(...)`:

```java
public List<TicketResponse> getTicketsBatch(List<Long> ids) {
    return ids.stream()
            .map(this::getById)
            .toList();
}
```

Для цього self-invocation анотація `@MaskSensitiveData` на `getById` **не спрацьовує**. Якщо `getById` викликати з іншого Spring-компонента через проксі, `@Around` застосовується. Щоб маскування працювало і для пакетного методу, його можна викликати через окремий Spring-компонент/сервіс або винести спільну логіку в окремий bean і викликати через нього.

## Підсумок зон дії

- **Before (`@Before`)** — `ProtectSystemEntityAspect`: перевірка перед методом; заборонений ID зупиняє виконання винятком.
- **After (`@AfterReturning`)** — `AuditActionAspect`: аудит після успішного методу; виняткове завершення не аудитується цим advice.
- **Around (`@Around`)** — `MaskSensitiveDataAspect`: контролює виклик цілком, виконує початковий метод і за потреби змінює його результат перед поверненням.

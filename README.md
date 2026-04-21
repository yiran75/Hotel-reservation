# Hotel Management System

A desktop application built in **Java** for hotel staff to manage guests, rooms, and reservations. All data is saved to binary files on disk — no database required. The application is built around the **MVC architecture** and demonstrates **9 classic design patterns** split across three team members.

---

## Table of Contents

- [Overview](#overview)
- [How to Run](#how-to-run)
- [Project Structure](#project-structure)
- [Architecture — MVC](#architecture--mvc)
- [The 3 Entities](#the-3-entities)
- [Data Storage](#data-storage)
- [Design Patterns](#design-patterns)
  - [Member A — Creational](#member-a--creational-patterns)
  - [Member B — Structural](#member-b--structural-patterns)
  - [Member C — Behavioral](#member-c--behavioral-patterns)
- [Team & Contributions](#team--contributions)

---

## Overview

| | |
|---|---|
| **Language** | Java SE 8+ |
| **UI Toolkit** | Java Swing |
| **Storage** | Sequential-access binary `.dat` files (Java Serialization) |
| **Architecture** | MVC (Model – View – Controller) |
| **Design Patterns** | 9 patterns across Creational, Structural, and Behavioral categories |
| **Run with** | `java -jar HotelManagementSystem.jar` |

The application has four tabs when launched:

- **Guests** — Register, edit, and delete hotel customers
- **Rooms** — Manage Standard, Deluxe, and Suite rooms
- **Reservations** — Book rooms for guests with date pickers and auto-calculated pricing
- **Dashboard** — Live record counts and architecture summary

---

## How to Run

**Requirements:** Java 8 or later installed on your machine.

```bash
# Option 1 — Run the JAR directly
java -jar HotelManagementSystem.jar

# Option 2 — Compile and run from source
javac -d out -sourcepath src src/view/MainFrame.java
java -cp out view.MainFrame
```

A `data/` folder will be created automatically in the same directory as the JAR on first run. This is where the three `.dat` files are stored. Sample data is seeded automatically so the application is not empty on first launch.

---

## Project Structure

```
src/
 ├── adapter/
 │    └── DateAdapter.java              Pattern 6  — Adapter
 ├── builder/
 │    └── ReservationBuilder.java       Pattern 3  — Builder
 ├── command/
 │    ├── Command.java                  Pattern 8  — Command (interface)
 │    ├── CommandInvoker.java           Pattern 8  — Command (invoker)
 │    ├── CustomerCommands.java         Pattern 8  — Add / Edit / Delete customer
 │    ├── RoomCommands.java             Pattern 8  — Add / Edit / Delete room
 │    └── ReservationCommands.java      Pattern 8  — Add / Edit / Delete reservation
 ├── dao/
 │    ├── CustomerDAO.java              Reads and writes customers.dat
 │    ├── RoomDAO.java                  Reads and writes rooms.dat
 │    └── ReservationDAO.java           Reads and writes reservations.dat
 ├── decorator/
 │    └── RoomDecorator.java            Pattern 5  — Decorator
 ├── enums/
 │    ├── IdType.java                   PASSPORT · NATIONAL_ID · DRIVERS_LICENSE
 │    ├── ReservationStatus.java        PENDING · CONFIRMED · CHECKED_IN · CHECKED_OUT · CANCELLED
 │    ├── RoomStatus.java               AVAILABLE · OCCUPIED · MAINTENANCE · RESERVED
 │    └── RoomType.java                 STANDARD · DELUXE · SUITE · PENTHOUSE
 ├── facade/
 │    └── HotelBookingFacade.java       Pattern 4  — Facade
 ├── factory/
 │    └── RoomFactory.java              Pattern 2  — Factory Method
 ├── model/
 │    ├── Customer.java                 Entity — implements Serializable
 │    ├── Room.java                     Entity — abstract, implements Serializable
 │    ├── StandardRoom.java             Subclass of Room
 │    ├── DeluxeRoom.java               Subclass of Room
 │    ├── SuiteRoom.java                Subclass of Room
 │    └── Reservation.java              Link entity — implements Serializable
 ├── observer/
 │    ├── DataObservable.java           Pattern 7  — Observer (interface for DAOs)
 │    └── DataObserver.java             Pattern 7  — Observer (interface for TableModels)
 ├── singleton/
 │    └── HotelDatabaseManager.java     Pattern 1  — Singleton
 ├── strategy/
 │    └── PricingStrategy.java          Pattern 9  — Strategy (5 concrete strategies)
 └── view/
      ├── MainFrame.java                Main window — JFrame with tabbed navigation
      ├── UITheme.java                  Centralised dark theme and component factories
      ├── CustomerForm.java             Guest management form + JTable
      ├── RoomForm.java                 Room management form + CardLayout sub-panels
      ├── ReservationForm.java          Booking form + live ID lookup + auto-calculation
      ├── CustomerTableModel.java       AbstractTableModel + DataObserver
      ├── RoomTableModel.java           AbstractTableModel + DataObserver
      └── ReservationTableModel.java    AbstractTableModel + DataObserver

data/                                   Auto-created at runtime
 ├── customers.dat
 ├── rooms.dat
 └── reservations.dat
```

---

## Architecture — MVC

The codebase is split into three layers that never cross-communicate directly.

```
┌──────────────────────────────────────────────────────────┐
│  VIEW  —  src/view/                                      │
│  Swing forms, JTables, JSpinners, JComboBoxes            │
│  Knows nothing about files. Talks only through Commands. │
└────────────────────┬─────────────────────────────────────┘
                     │ creates Command objects
┌────────────────────▼─────────────────────────────────────┐
│  CONTROLLER  —  src/command/  +  src/dao/                │
│  Commands wrap each action. DAOs read and write files.   │
│  DAOs notify TableModels when data changes (Observer).   │
└────────────────────┬─────────────────────────────────────┘
                     │ reads / writes
┌────────────────────▼─────────────────────────────────────┐
│  MODEL  —  src/model/  +  src/enums/                     │
│  Customer, Room (abstract), Reservation, four enums.     │
│  Pure data classes — no UI code anywhere.                │
└──────────────────────────────────────────────────────────┘
```

**Example — pressing "Save Guest":**

1. Button click fires in `CustomerForm` (View)
2. Form creates `new AddCustomerCommand(dao, customer)`
3. Hands it to `CommandInvoker.invoke()` (Command layer)
4. Command calls `CustomerDAO.save()` (DAO layer)
5. DAO writes to `customers.dat` then calls `notifyObservers()`
6. `CustomerTableModel.onDataChanged()` fires via `SwingUtilities.invokeLater()`
7. `fireTableDataChanged()` repaints the JTable automatically

The View never touched the file. The DAO never touched the JTable.

---

## The 3 Entities

### Customer
Represents a hotel guest. Fields: `customerId` (auto-increments from 1000), `firstName`, `lastName`, `email`, `phone`, `address`, `idType` (enum), `idNumber`, `dateOfBirth`, `registrationDate`.

### Room *(abstract)*
Cannot be instantiated directly. Shared fields: `roomId`, `roomNumber`, `roomType` (enum), `status` (enum), `pricePerNight`, `capacity`, `floor`, `description`. Two abstract methods — `getAmenities()` and `getRoomCategory()` — which each subclass must implement.

| Subclass | Extra fields |
|---|---|
| `StandardRoom` | `hasTv`, `hasWifi`, `bedType` |
| `DeluxeRoom` | `hasBalcony`, `hasMiniBar`, `hasJacuzzi`, `viewType` |
| `SuiteRoom` | `numberOfRooms`, `hasPrivatePool`, `hasConcierge`, `hasKitchenette` |

### Reservation *(link entity)*
Connects a Customer to a Room for a date range. Fields: `reservationId`, `customerId` (FK), `roomId` (FK), `checkInDate`, `checkOutDate`, `bookingDate`, `numberOfGuests`, `status` (enum), `totalAmount`, `depositAmount`, `specialRequests`. Utility method `getNumberOfNights()` calculates from the two dates automatically.

---

## Data Storage

All three entity classes implement `java.io.Serializable`. This allows Java to convert the entire object into bytes and write it to a file, then read it back and reconstruct the object exactly.

**Writing (save / update / delete):**
1. Load the full list from the `.dat` file using `ObjectInputStream`
2. Add, replace, or remove the relevant record
3. Overwrite the entire file using `ObjectOutputStream`

**Reading (load table on startup or refresh):**
Read objects one by one in a loop until `EOFException` is thrown — that signals the end of the sequential-access file.

**Rule:** Only DAO classes (`CustomerDAO`, `RoomDAO`, `ReservationDAO`) are ever allowed to open a file. No form or model class touches the filesystem directly.

---

## Design Patterns

### Member A — Creational Patterns

#### Pattern 1 — Singleton (`HotelDatabaseManager`)
Guarantees that exactly one instance of `HotelDatabaseManager` exists for the entire application lifetime. All three DAOs are owned by this manager, so every class shares the same DAO objects and therefore the same observer lists. Uses the **initialization-on-demand holder** idiom — lazy, thread-safe, zero synchronization cost.

```java
HotelDatabaseManager db = HotelDatabaseManager.getInstance();
CustomerDAO customerDAO = db.getCustomerDAO();
```

Without this, two parts of the app could create separate `CustomerDAO` objects with separate observer lists — the JTable on one would never hear saves from the other.

---

#### Pattern 2 — Factory Method (`RoomFactory`)
Removes the `if/else` chain from `RoomForm` that decided which `Room` subclass to construct. An abstract `RoomCreator` declares `createRoom()` as the factory method. Three concrete creators (`StandardRoomCreator`, `DeluxeRoomCreator`, `SuiteRoomCreator`) each override it. `RoomFactory.create(RoomType, ...)` picks the right creator from the enum.

```java
Room room = RoomFactory.create(RoomType.DELUXE, id, number, price, cap, floor, desc);
```

Adding a new room type (e.g. `PenthouseRoom`) means adding one new creator class — zero changes to any form or DAO.

---

#### Pattern 3 — Builder (`ReservationBuilder`)
Builds a `Reservation` step by step using a fluent chain. A `Reservation` has 10+ fields — without the Builder, a transposed argument in the constructor silently produces corrupt data (e.g. swapping `numberOfGuests` and `totalAmount` — both are numbers, Java won't catch it). The Builder's `build()` method validates all fields before constructing the object.

```java
Reservation res = new ReservationBuilder()
    .forCustomer(customerId)
    .inRoom(roomId)
    .checkIn(checkInDate)
    .checkOut(checkOutDate)
    .withGuests(2)
    .withSpecialRequests("Late check-in")
    .applyPricing(room.getPricePerNight(), strategy)   // connects to Strategy pattern
    .build();
```

---

### Member B — Structural Patterns

#### Pattern 4 — Facade (`HotelBookingFacade`)
Hides the complexity of a full booking operation behind a single method call. Internally it validates the customer exists, validates the room exists, checks availability, builds the reservation via the Builder, saves it, and updates the room status — six steps in the correct order with proper error handling. Returns an immutable `BookingResult` so no raw exceptions reach the UI.

```java
BookingResult result = facade.bookRoom(
    customerId, roomId, checkIn, checkOut, guests, strategy, requests
);
if (result.isSuccess()) showMessage("Booked! Total: $" + result.getTotalCharged());
```

---

#### Pattern 5 — Decorator (`RoomDecorator`)
Wraps a `Room` at runtime to add optional paid add-ons without modifying the original class. Decorators stack — each layer adds its own cost to whatever the layer below it returns.

| Decorator | Cost type | Amount |
|---|---|---|
| `SpaAccessDecorator` | Per night | +$50 |
| `BreakfastDecorator` | Per night | +$30 |
| `AirportTransferDecorator` | Flat fee | +$75 |
| `LateCheckoutDecorator` | Flat fee | +$40 |

```java
Room room = new BreakfastDecorator(new SpaAccessDecorator(deluxeRoom));
room.getPricePerNight();   // 240 + 50 + 30 = 320
room.getAmenities();       // "..., Spa Access, Breakfast Package"
```

Without Decorator you would need a separate subclass for every combination — 45+ classes for 3 room types × 4 add-ons. Decorator solves it with 7 classes total.

---

#### Pattern 6 — Adapter (`DateAdapter`)
Converts `java.util.Date` from the `JSpinner` date picker into a timezone-safe `SerializableDate` before saving to file. `java.util.Date` is actually a point in time measured in milliseconds, interpreted through a timezone. When a `.dat` file created on a computer in Malaysia (UTC+8) is opened on a computer in the UK (UTC+0), the date silently shifts. `SerializableDate` stores only three plain integers — year, month, day — immune to timezone drift.

```java
// Before saving to file
SerializableDate safe = DateAdapter.toSerializable((Date) spnCheckIn.getValue());

// When loading back into the JSpinner
spnCheckIn.setValue(DateAdapter.toDate(safe));
```

---

### Member C — Behavioral Patterns

#### Pattern 7 — Observer (`DataObserver` / `DataObservable`)
The three DAOs implement `DataObservable` and call `notifyObservers()` after every write. The three TableModels implement `DataObserver` and register themselves on the DAO at construction. When notified, each TableModel reloads data from the DAO and calls `fireTableDataChanged()` via `SwingUtilities.invokeLater()` — keeping Swing's Event Dispatch Thread rule intact.

The JTable refreshes automatically after every save, edit, and delete — no button handler ever calls `refresh()` manually. Without Observer, forgetting to add one `refresh()` call anywhere would silently show stale data.

---

#### Pattern 8 — Command (`Command` / `CommandInvoker`)
Every button action (Save, Edit, Delete) is wrapped in a self-contained Command object instead of putting business logic inside the Swing `ActionListener`. The button creates the command and hands it to the shared `CommandInvoker`. The invoker calls `execute()` and records the result in a 50-entry history log.

**9 concrete command classes:**

| Group | Commands |
|---|---|
| `CustomerCommands` | `AddCustomerCommand`, `EditCustomerCommand`, `DeleteCustomerCommand` |
| `RoomCommands` | `AddRoomCommand`, `EditRoomCommand`, `DeleteRoomCommand` |
| `ReservationCommands` | `AddReservationCommand`, `EditReservationCommand`, `DeleteReservationCommand` |

```java
// The entire button handler — no business logic in the UI
btnSave.addActionListener(e -> {
    boolean ok = invoker.invoke(new AddCustomerCommand(dao, customer));
    setStatus(ok ? "Saved." : "Failed.", ok);
});
```

---

#### Pattern 9 — Strategy (`PricingStrategy`)
Defines a family of interchangeable pricing algorithms. The `ReservationForm` has a dropdown where the user selects a strategy. The same `calculateTotal(basePrice, nights)` call is made regardless of which strategy is active — swapping the strategy changes the result without touching any other code.

| Strategy | Formula |
|---|---|
| `StandardPricingStrategy` | `basePrice × nights` |
| `HolidayPricingStrategy` | `basePrice × nights × 1.30` (+30%) |
| `OffSeasonPricingStrategy` | `basePrice × nights × 0.80` (−20%) |
| `LastMinutePricingStrategy` | `basePrice × nights × 0.85` (−15%) |
| `WeekendPricingStrategy` | weekday nights at base + weekend nights at base × 1.10 |

Adding a new pricing rule means adding one new class that implements `PricingStrategy` — zero changes to the form, the DAO, or any other existing class.

---

## Team & Contributions

| Member | Patterns | Package |
|---|---|---|
| **Member A** | Singleton, Factory Method, Builder | `singleton/`, `factory/`, `builder/` |
| **Member B** | Facade, Decorator, Adapter | `facade/`, `decorator/`, `adapter/` |
| **Member C** | Observer, Command, Strategy | `observer/`, `command/`, `strategy/` |
| **Leader** | Base MVC system, DAO layer, Swing forms, Git merges | `model/`, `dao/`, `view/`, `enums/` |

---

## License

This project was developed as a university assignment demonstrating Java MVC architecture and object-oriented design patterns.

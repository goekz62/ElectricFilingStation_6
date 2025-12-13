# Electric Filling Station Network - Group 6

Welcome to the **Electric Filling Station Network** project. This repository hosts the Java implementation of a management system for electric vehicle charging infrastructure.

## Team Members
* **Serhat Göktas**
* **Jelena Mladenovic**
* **Nisa Yesilik**

---

## Project Vision

The system is designed to handle:
* **Infrastructure Management:** Managing Locations and specific AC/DC Charging Points.
* **Customer Services:** Registration, prepaid account management, and payment methods.
* **Charging Operations:** Real-time tracking of charging sessions (time & energy).
* **Billing & Finance:** Automated invoice generation and balance management.

## System Architecture
The application follows a modular domain-driven structure, separating business objects from management logic:

### Core Components
* **`ElectricFillingStationNetwork`**: The main entry point controlling the system.
* **Managers**:
    * `LocationManager`: Handles creation and updates of `Location` and `ChargingPoint`.
    * `CustomerManager`: Manages `Customer` data and accounts.
    * `ChargingSessionManager`: Controls active and past `ChargingSession`s.
    * `InvoiceManager`: Generates `Invoice` objects based on session data.

### Business Objects
The data model includes `Location`, `ChargingPoint` (AC/DC), `Customer`, `ChargingSession`, and `Invoice`.

---

## Current Status: MVP 1
> **Note to Reviewers:** The current `master` branch reflects the status of **MVP Part 1**.

**Implemented Features:**
* ✅ **Project Setup:** Repository structure, Maven/Gradle configuration.
* ✅ **Domain Model:** All business objects and manager classes are implemented (skeletons & attributes).
* ✅ **Data Structure:** Relationships between Locations, Charging Points, and Customers are defined.
* ✅ **User Stories:** Backlog and Ready columns are prepared in the Project Board.

**Upcoming Features (MVP 2):**
* 🚧 Implementation of charging logic (start/stop session).
* 🚧 Dynamic calculation of prices and invoice generation.


---

## 🛠️ Tech Stack
* **Language:** Java
* **IDE:** IntelliJ IDEA
* **Version Control:** Git & GitHub
* **Modeling:** UML / Modelio

# Smart Library System

![license](https://img.shields.io/badge/license-MIT-blue)
![vue](https://img.shields.io/badge/Vue-3.5-42b883)
![spring boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-6db33f)
![java](https://img.shields.io/badge/Java-17-orange)
![mysql](https://img.shields.io/badge/MySQL-8.0-4479a1)

[简体中文](README.md) | English

## Overview

Smart Library System is a full-stack, decoupled application built for university library scenarios. It collapses the three most time-consuming tasks in a real library, finding a book, claiming a study seat, and locating a shelf on a specific floor, into a single web interface. Readers can search the catalogue, borrow and renew online, reserve study seats from a visual seat map, follow a floor plan to the exact shelf, and track reading time and notes in a personal study centre. Administrators work from a separate console that manages the catalogue, reviews return requests, and monitors reader accounts and violation records.

The engineering focus is real-time consistency of resource state. Seats move between free, occupied, and temporarily away, and a backend scheduler scans them every minute. A reservation that is not checked in within 15 minutes, or a seat left temporarily for more than 15 minutes, is released automatically and counted as a violation. Three violations disable reservation privileges for seven days. Borrowing follows an equally closed loop: a reader submits a return request, the book enters a pending-review state, and stock is only restored after an administrator confirms, so the inventory count never drifts from what is actually on the shelf.

On the security side, passwords are stored as irreversible BCrypt hashes, tokens are issued as JWTs and validated in an interceptor, and the role is read from the `role` column rather than trusted from the client. Access control is enforced twice: once by the frontend route guard and once by the backend endpoint check. The database connection, JWT secret, and CORS allowlist are all externalized to environment variables, with local development defaults kept in the configuration file, so deployment requires no code changes.

Two runtime profiles ship with the project. The **demo profile** runs on an H2 file database in MySQL compatibility mode, which means the whole system starts without installing MySQL, making it suitable for classroom demos, live reviews, and small-scale intranet trials. The **production profile** switches to MySQL 8 and is designed for Nginx, Docker, or systemd deployment. A one-click Windows script handles Java detection, packaging when the jar is missing, secret generation, starting both services, and printing the LAN address.

## Tech Stack

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| Frontend framework | Vue | 3.5 | Composition API and single-file components |
| Language | TypeScript | 6.0 | End-to-end type coverage |
| Build tool | Vite | 8.0 | Dev server and production build |
| UI components | Element Plus + @element-plus/icons-vue | 2.14 / 2.3 | Forms, tables, dialogs, and base widgets |
| Admin template | Tabler Core | 1.7 | Page shell and visual system, bridged through CSS |
| State management | Pinia | 3.0 | User session and global state |
| Routing | Vue Router | 4.6 | Route guards and role-based page access |
| HTTP client | Axios | 1.17 | Request wrapper, interceptors, unified error handling |
| Charts | ECharts | 5.6 | Borrowing trends and monthly reading reports |
| Interactive graphics | Native SVG | n/a | Floor plans and seat maps |
| Style preprocessing | Sass (sass-embedded) | 1.100 | Theme variables and nested styles |
| Backend framework | Spring Boot | 3.2.0 | Web layer, task scheduling, bean validation |
| Runtime | Java (JDK) | 17 | Backend runtime |
| Persistence | MyBatis-Plus | 3.5.5 | Generic CRUD, pagination, logical delete |
| Authentication | java-jwt (Auth0) | 4.4.0 | Token signing and verification |
| Password hashing | spring-security-crypto | Boot managed | BCrypt without the full filter chain |
| Validation | spring-boot-starter-validation | Boot managed | Request body constraint checks |
| Scheduling | Spring Scheduling | Boot managed | Seat and reservation state scans |
| Boilerplate reduction | Lombok | Boot managed | Entity accessor generation |
| Database (production) | MySQL | 8.0 | Primary database, utf8mb4 |
| Database (demo) | H2 Database | Boot managed | File-based database, MySQL compatibility mode |
| Deployment | Nginx / Docker / Docker Compose / systemd | n/a | Static hosting and container orchestration |

Exact patch versions are authoritative in `smart-library-frontend/package.json` and `smart-library-backend/pom.xml`.

## Requirements

### Hardware and OS

| Item | Requirement |
|---|---|
| Operating system | Windows 10/11, macOS, or Linux |
| Memory | 4 GB or more recommended, the frontend build is the heaviest step |
| Disk | About 300 MB for the demo profile, plus MySQL data space in production |

### Software dependencies

| Software | Minimum version | Notes |
|---|---|---|
| JDK | 17 | Compiles and runs the backend, verify with `java -version` |
| Node.js | 20.19 or 22.12+ | Constrained by the `engines` field of Vite 8, Node 18 will not start |
| npm | Bundled with Node | Installs frontend dependencies |
| Maven | 3.8 | Backend build, the Maven bundled with IntelliJ works too |
| MySQL | 8.0 | Production only, skippable in the demo profile |
| Docker | 24+ | Containerized deployment only |

### Ports

| Port | Service | Notes |
|---|---|---|
| 3000 | Frontend dev server | Fixed at 3000 in `vite.config.ts`, bound to `0.0.0.0` |
| 8080 | Backend API | The frontend proxies the `/api` prefix to this port |
| 3306 | MySQL | Production profile |
| 5173 / 80 | Docker frontend mapping | `docker-compose.yml` maps container port 80 to host port 5173 |

## Local Setup

### Option 1: one-click script (Windows, zero configuration)

This path targets anyone opening the project for the first time who simply wants to see it running. The backend uses the H2 demo database and does not need MySQL.

| File | Purpose |
|---|---|
| `start-library.bat` | Starts backend and frontend, prints local and LAN addresses |
| `stop-library.bat` | Stops both services and frees ports 3000 and 8080 |
| `allow-firewall.bat` | Opens ports 3000/8080 in the firewall, needs administrator rights |
| `update-github.bat` | Commits and pushes local changes to GitHub |

Running `start-library.bat` executes these steps in order: detect the Java installation, package with Maven if the jar is missing, generate a random `JWT_SECRET` on first run and persist it to `.local\jwt-secret.txt`, start the backend with the `demo` profile in the background, install frontend dependencies and start the dev server, then print the access addresses. Logs go to the `logs\` directory.

Open `http://localhost:3000` once the script reports success. Phones and other computers on the same Wi-Fi can use the printed LAN address, for example `http://192.168.1.10:3000`.

The script prints the built-in administrator account `admin` / `admin`. Change that password from the profile page right after the first login. Treat `.local\jwt-secret.txt` as a secret and keep it out of the repository; deleting it invalidates every issued token and a new one is generated on the next start.

### Option 2: manual start (development)

**Step 1: prepare the database**

The demo profile needs no preparation. The backend creates an H2 file database under `smart-library-backend/data/` and applies `db/schema-h2.sql` automatically.

The production profile uses MySQL:

```bash
# Run from the repository root, the script creates the schema and tables
mysql -u root -p < smart-library-backend/src/main/resources/db/schema.sql
```

`schema.sql` begins with `DROP TABLE IF EXISTS` statements, so running it again wipes existing data. Use it for initialization or a clean reset only.

**Step 2: start the backend**

```bash
cd smart-library-backend

# Demo profile: H2 file database, no MySQL needed
mvn spring-boot:run -Dspring-boot.run.profiles=demo

# Production profile: reads the MySQL configuration
mvn spring-boot:run

# Or package first and run the jar
mvn clean package -DskipTests
java -jar target/smart-library-backend-1.0.0.jar --spring.profiles.active=demo
```

The backend listens on `http://localhost:8080`. In the demo profile the H2 console is available at `http://localhost:8080/h2-console` with JDBC URL `jdbc:h2:file:./data/smart_library`, user `sa`, and an empty password.

**Step 3: start the frontend**

```bash
cd smart-library-frontend
npm install
npm run dev
```

The dev server listens on `http://localhost:3000`. Vite proxies `/api` to `http://localhost:8080`, so frontend request paths are always prefixed with `/api`.

Production build:

```bash
npm run build      # outputs to dist/
npm run preview    # preview the build locally
```

**Step 4: sign in**

Open `http://localhost:3000` and use `admin` / `admin` to enter the admin console. Reader accounts self-register on the login page and must use an 11-digit student ID.

### Option 3: Docker Compose

```bash
docker compose up -d --build
```

This starts three containers: MySQL 8.0 (initialized from `schema.sql`), the Spring Boot backend on 8080, and an Nginx-hosted frontend on host port 5173. Open `http://localhost:5173`.

Container details and the Alibaba Cloud walkthrough are in [DOCKER_GUIDE.md](DOCKER_GUIDE.md) and [deploy/DEPLOY_ALIYUN.md](deploy/DEPLOY_ALIYUN.md). These documents are written in Chinese.

## Environment Variables

The database connection, token secret, and CORS allowlist are externalized. When a variable is unset the default in brackets applies, so local development needs no configuration at all; deployments only inject variables instead of editing files.

| Variable | Purpose | Default |
|---|---|---|
| `DB_URL` | MySQL connection string | `jdbc:mysql://localhost:3306/smart_library?...` |
| `DB_USERNAME` | Database user | `root` |
| `DB_PASSWORD` | Database password | `root` |
| `JWT_SECRET` | Token signing secret, must be replaced before going live | `smart-library-secret-key-2024-very-long-and-secure` |
| `JWT_EXPIRATION` | Token lifetime in milliseconds | `86400000` (24 hours) |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins, comma separated | `*` |

```powershell
# Windows PowerShell
$env:DB_PASSWORD = "your-real-db-password"
$env:JWT_SECRET  = "a-long-enough-random-string"
java -jar target/smart-library-backend-1.0.0.jar
```

```bash
# Linux / macOS
export DB_PASSWORD="your-real-db-password"
export JWT_SECRET="a-long-enough-random-string"
java -jar target/smart-library-backend-1.0.0.jar
```

Rotating `JWT_SECRET` invalidates every previously issued token, and all users must sign in again.

## Core Features

### Reader side

| Module | Capabilities |
|---|---|
| Dashboard | Active loans, due-date reminders, cumulative reading time, and shortcuts |
| Catalogue search | Multi-field search by title, author, ISBN, and category, with pagination and live stock status |
| Book detail | Shelf location, summary, stock and borrowing popularity, plus bookmark, borrow, and note actions |
| Borrowing | Borrow, renew, and submit return requests, with loan period and countdown to due date |
| Seat reservation | SVG seat map with live colour coding, click a seat to reserve, check in, step away, or release |
| Indoor navigation | Floor plans with shelf markers and turn-by-turn routes from an origin to a destination |
| Reading lists | Browse curated lists, like them, and add an entire list to the reading queue |
| Study centre | Reading check-ins, reading statistics, note management, and monthly reading reports |

### Admin side

| Module | Capabilities |
|---|---|
| Admin dashboard | Aggregate view of total holdings, active loans, reader count, and reservation activity |
| Catalogue management | Create and edit books, maintain total copies and available copies |
| Borrowing management | Approve reader return requests, review overdue records, and send due reminders |
| Reader management | Reader list with violation counts and reservation ban status |

### Business rules

Borrowing allows at most five different books at a time per reader, and the same title cannot be borrowed twice concurrently. A loan period can be set between 12 and 720 hours, defaulting to 30 days. Each book can be renewed once, extending the due date by 30 days. A return request stays pending until an administrator approves it, and only then is stock restored.

The three seat states are maintained by a backend scan that runs every minute. A reservation not checked in within 15 minutes, or a seat left temporarily for more than 15 minutes, is released and recorded as a violation. After three violations the reader loses reservation privileges for seven days.

### Permission model

The system has exactly two roles, `ADMIN` and `USER`, stored in the `sys_user.role` column and returned with the token at login. The frontend route guard blocks navigation using `adminOnly` and `studentOnly` route metadata, while the backend `JwtInterceptor` validates again and writes `userId` and `role` into request attributes, which endpoints then use to scope their data. The two feature sets are mutually exclusive: administrators are not given borrowing or renewal capabilities.

## Screenshots

All screenshot files live in `docs/screenshots/` and follow the names below; the gallery further down renders them in order. Demo data comes from the `admin` account plus one reader account, captured at a 1440 px browser width from the top of each page.

| No. | Screen | Route | File |
|---|---|---|---|
| 01 | Login | `/login` | `docs/screenshots/01-login.png` |
| 02 | Student dashboard | `/dashboard` | `docs/screenshots/02-dashboard.png` |
| 03 | Catalogue search | `/books` | `docs/screenshots/03-books.png` |
| 04 | Book detail | `/books/:id` | `docs/screenshots/04-book-detail.png` |
| 05 | Borrowing | `/borrow` | `docs/screenshots/05-borrow.png` |
| 06 | Seat reservation | `/seats` | `docs/screenshots/06-seats.png` |
| 07 | Indoor navigation | `/navigation` | `docs/screenshots/07-navigation.png` |
| 08 | Reading lists | `/book-lists` | `docs/screenshots/08-book-lists.png` |
| 09 | Study centre | `/study-center` | `docs/screenshots/09-study-center.png` |
| 10 | Admin dashboard | `/admin` | `docs/screenshots/10-admin-dashboard.png` |
| 11 | Reader management | `/users` | `docs/screenshots/11-users.png` |

### 01 Login

![Login](docs/screenshots/01-login.png)

### 02 Student dashboard

![Student dashboard](docs/screenshots/02-dashboard.png)

### 03 Catalogue search

![Catalogue search](docs/screenshots/03-books.png)

### 04 Book detail

![Book detail](docs/screenshots/04-book-detail.png)

### 05 Borrowing

![Borrowing](docs/screenshots/05-borrow.png)

### 06 Seat reservation

![Seat reservation](docs/screenshots/06-seats.png)

### 07 Indoor navigation

![Indoor navigation](docs/screenshots/07-navigation.png)

### 08 Reading lists

![Reading lists](docs/screenshots/08-book-lists.png)

### 09 Study centre

![Study centre](docs/screenshots/09-study-center.png)

### 10 Admin dashboard

![Admin dashboard](docs/screenshots/10-admin-dashboard.png)

### 11 Reader management

![Reader management](docs/screenshots/11-users.png)

All screenshots were captured at a 1440 px browser width while signed in, with the bookmarks bar hidden, using the local H2 demo database.

## Project Structure

```
smart-library/
├── smart-library-frontend/          Frontend
│   ├── src/
│   │   ├── api/                    Request wrappers, split by domain
│   │   ├── components/             SVG seat map, floor plan, wheel picker, monthly report
│   │   ├── constants/              Role constants
│   │   ├── mock/                   Local mock adapters
│   │   ├── router/                 Route table and role guards
│   │   ├── store/                  Pinia user session
│   │   ├── styles/                 Tabler and Element Plus style bridge
│   │   ├── utils/                  HTTP wrapper, live polling
│   │   └── views/                  14 page views
│   ├── vite.config.ts              Ports, proxy, chunk splitting
│   └── package.json
├── smart-library-backend/           Backend
│   ├── src/main/java/com/smartlibrary/
│   │   ├── config/                 CORS, MyBatis-Plus, password encoder, scheduling, migration
│   │   ├── controller/             11 REST controllers
│   │   ├── dto/                    Request and response objects
│   │   ├── entity/                 11 entity classes
│   │   ├── exception/              Business exceptions and global handler
│   │   ├── interceptor/            JWT interceptor
│   │   ├── job/                    Reservation and away-timeout scans
│   │   ├── mapper/                 MyBatis-Plus mappers
│   │   ├── service/                Business logic
│   │   └── utils/                  Token utilities
│   └── src/main/resources/
│       ├── db/schema.sql           MySQL DDL
│       ├── db/schema-h2.sql        H2 DDL
│       ├── application.yml         Production configuration
│       └── application-demo.yml    Demo configuration
├── deploy/                          Alibaba Cloud notes, Nginx config, systemd unit
├── docs/screenshots/                Screenshots
├── scripts/                         Demo and data-cleanup scripts
├── docker-compose.yml               Three-container orchestration
├── start-library.bat                One-click start
├── stop-library.bat                 One-click stop
├── allow-firewall.bat               Firewall rules
└── update-github.bat                Push to GitHub
```

## FAQ

**Backend fails with `Communications link failure`**

MySQL is not running or the connection settings are wrong. Confirm the MySQL service is up, then check `DB_URL` and the credentials. If you only want to see the system running, start with `--spring.profiles.active=demo` to use H2 and skip MySQL entirely.

**Frontend login reports `Network Error`**

The backend is not running or the proxy is broken. Verify that something listens on port 8080, check the `/api` proxy target in `vite.config.ts`, and confirm `CORS_ALLOWED_ORIGINS` allows the frontend origin.

**A reserved seat is never released**

The scan depends on `@EnableScheduling`. Check that the annotation is present on `SchedulingConfig` and look for the reservation timeout entry in the backend log. The scan runs once per minute.

**The administrator password is lost**

Passwords are BCrypt hashes and cannot be reversed. In the demo profile, delete `smart-library-backend/data/smart_library.mv.db` and restart: the database is recreated with `admin` / `admin`, but existing business data is lost as well. In production, re-run the admin `INSERT` from `schema.sql`.

**A phone on the local network cannot connect**

Confirm the phone and the computer are on the same Wi-Fi, then run `allow-firewall.bat` as administrator to open ports 3000 and 8080. When Windows shows the firewall prompt for the first time, choose to allow access.

## Documentation

| Document | Contents |
|---|---|
| [DEMO_START_GUIDE.md](DEMO_START_GUIDE.md) | Demo startup and walkthrough script |
| [DESIGN.md](DESIGN.md) | Visual system and design decisions |
| [DOCKER_GUIDE.md](DOCKER_GUIDE.md) | Containerized build and run |
| [deploy/DEPLOY_ALIYUN.md](deploy/DEPLOY_ALIYUN.md) | Alibaba Cloud deployment steps |
| [MOCK_MODE_GUIDE.md](MOCK_MODE_GUIDE.md) | Frontend-only mock development |
| [NEW_PRD_CHANGELOG.md](NEW_PRD_CHANGELOG.md) | Requirement change log |

These documents are currently written in Chinese.

## License

Released under the [MIT License](LICENSE). Free to use, modify, and redistribute, provided the copyright notice is retained.

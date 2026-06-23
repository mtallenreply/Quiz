# Modul 20: Oracle Cloud – OCI, GraalVM und Cloud-native Java

## Übersicht

Oracle Cloud Infrastructure (OCI) bietet eine vollstaendige Plattform fuer Java-Anwendungen – von der JDK-Auswahl ueber GraalVM Native Image bis zu Cloud-nativen Frameworks wie Helidon. Dieses Modul vermittelt die Grundlagen fuer den Betrieb von Java-Anwendungen in der Oracle Cloud.

| Abschnitt            | Dauer |
|----------------------|-------|
| OCI Overview         | 38 m  |
| Oracle JDK and GraalVM | 22 m |
| Native Image         | 25 m  |
| Helidon Framework    | 20 m  |
| Containerization     | 15 m  |
| OCI Deployment       | 18 m  |
| Practice 20-1        | 20 m  |
| **Gesamt**           | **158 m** |

> **Skill Check: Oracle Cloud** – mind. 80 % erforderlich, um das Modul abzuschließen.

---

## 1. Oracle Cloud Infrastructure (OCI) Übersicht

### 1.1 OCI-Kernkonzepte

OCI ist Oracles Public-Cloud-Plattform und bietet Compute, Storage, Networking, Datenbank und Entwicklerdienste.

| OCI-Ressource                 | Beschreibung                                                        |
|-------------------------------|---------------------------------------------------------------------|
| **Compartment**               | Logische Gruppe von OCI-Ressourcen (vergleichbar mit Ordner)        |
| **Availability Domain (AD)**  | Unabhaengiges Rechenzentrum innerhalb einer Region                  |
| **Fault Domain (FD)**         | Untergruppe einer AD; schuetzt vor Hardware-Ausfaellen              |
| **Virtual Cloud Network**     | Software-definiertes Netzwerk in OCI                                |
| **Compute Instance**          | Virtuelle Maschine oder Bare-Metal-Server                           |
| **Oracle Container Engine**   | Verwalteter Kubernetes-Dienst (OKE)                                 |
| **Container Instances**       | Serverless-Container (keine K8s-Kenntnisse noetig)                  |
| **OCI Functions**             | Serverless-Funktionen (FaaS) auf Basis von Fn Project               |
| **Object Storage**            | S3-kompatible Objektspeicherung                                     |
| **Autonomous Database**       | Selbstverwaltende Oracle-Datenbank                                  |
| **OCI DevOps**                | CI/CD-Pipeline-Dienst                                               |

### 1.2 Free Tier – Kostenfreie OCI-Ressourcen

| Ressource                     | Free-Tier-Kontingent                                                |
|-------------------------------|---------------------------------------------------------------------|
| Compute (ARM)                 | 4 OCPUs, 24 GB RAM (Always Free)                                    |
| Compute (AMD)                 | 2 VMs mit je 1/8 OCPU, 1 GB RAM                                     |
| Object Storage                | 20 GB                                                               |
| Autonomous Database           | 2 Instanzen mit je 20 GB                                            |
| OCI Container Registry        | 500 MB                                                              |
| Load Balancer                 | 1 Instanz, 10 Mbps                                                  |

### 1.3 OCI SDK fuer Java

```xml
<!-- Maven-Abhaengigkeit fuer OCI Java SDK -->
<dependency>
    <groupId>com.oracle.oci.sdk</groupId>
    <artifactId>oci-java-sdk-core</artifactId>
    <version>3.x.x</version>
</dependency>
```

```java
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;

public class OciObjectStorageBeispiel {

    public static void main(String[] args) throws Exception {
        // Authentifizierung ueber ~/.oci/config
        var auth = new ConfigFileAuthenticationDetailsProvider("DEFAULT");

        try (var client = ObjectStorageClient.builder().build(auth)) {
            var request = GetObjectRequest.builder()
                .namespaceName("mein-namespace")
                .bucketName("mein-bucket")
                .objectName("datei.txt")
                .build();

            GetObjectResponse response = client.getObject(request);
            String inhalt = new String(response.getInputStream().readAllBytes());
            System.out.println("Inhalt: " + inhalt);
        }
    }
}
```

---

## 2. Oracle JDK, OpenJDK und GraalVM JDK

### 2.1 JDK-Varianten im Vergleich

| JDK-Variante          | Herausgeber       | Lizenz              | Besonderheiten                                  |
|-----------------------|-------------------|---------------------|-------------------------------------------------|
| Oracle JDK            | Oracle            | NFTC / Kommerziell  | Support bis 2032+; OCI-integriert               |
| OpenJDK               | OpenJDK Community | GPL v2 + CPE        | Referenzimplementierung; kein LTS-Support       |
| GraalVM Community     | Oracle            | GPL v2 + CPE        | Native Image; Mehrsprachigkeit (frei)           |
| GraalVM Enterprise    | Oracle            | Kommerziell         | Optimierter JIT, Enterprise-Support             |
| GraalVM for OCI       | Oracle            | Kostenlos in OCI    | GraalVM Enterprise-Features in OCI             |
| Liberica JDK          | BellSoft          | GPL v2              | Alpine Linux (musl libc) unterstuetzt          |
| Eclipse Temurin       | Adoptium          | GPL v2              | Community-build von OpenJDK                     |
| Amazon Corretto       | Amazon            | GPL v2              | Optimiert fuer AWS; LTS-Support                |
| Microsoft Build OpenJDK | Microsoft       | GPL v2              | Optimiert fuer Azure                           |

### 2.2 GraalVM – Architektur

```
┌─────────────────────────────────────────────────────┐
│                  GraalVM JDK                        │
│                                                     │
│  ┌───────────────────┐  ┌───────────────────────┐  │
│  │   JIT Compiler    │  │   AOT Compiler        │  │
│  │   (Graal JIT)     │  │   (Native Image)      │  │
│  │   Laufzeit-JIT    │  │   Build-Zeit-Kompil.  │  │
│  └───────────────────┘  └───────────────────────┘  │
│                                                     │
│  ┌────────────────────────────────────────────────┐ │
│  │          Truffle Language Framework            │ │
│  │  JavaScript  Python  Ruby  R  LLVM  Wasm      │ │
│  └────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
```

---

## 3. GraalVM Native Image – AOT-Kompilierung

### 3.1 Was ist Native Image?

Native Image kompiliert Java-Bytecode **ahead-of-time (AOT)** in eine native Binaerdatei (`.exe` unter Windows, ELF unter Linux). Es ist kein JVM-Start mehr noetig.

| Eigenschaft                | JVM (HotSpot)         | Native Image               |
|----------------------------|-----------------------|----------------------------|
| Startzeit                  | 500ms – 5s            | 5ms – 50ms                 |
| Speicherverbrauch          | Hoch (JVM-Overhead)   | Gering (kein JVM-Overhead) |
| Peak-Leistung              | Sehr hoch (JIT)       | Etwas geringer (kein JIT)  |
| Erstellungszeit            | Schnell               | Langsam (Minuten)          |
| Reflexion                  | Vollstaendig           | Konfiguration noetig       |
| Dynamisches Klassladen     | Uneingeschraenkt       | Nicht unterstuetzt         |
| Containergroesse           | Gross (JRE + App)     | Klein (Binaer allein)      |

### 3.2 Native Image erstellen

```bash
# Vorraussetzung: GraalVM JDK installiert
java -version
# openjdk version "21.0.x" ... GraalVM CE ...

# native-image-Tool installieren (falls nicht im GraalVM-Bundle)
gu install native-image

# Einfaches JAR zu Native Image kompilieren
native-image -jar meine-app.jar meine-app-nativ

# Oder aus dem Klassenpfad
native-image -cp target/classes com.beispiel.Main meine-app-nativ
```

### 3.3 Maven-Plugin fuer Native Image

```xml
<plugin>
    <groupId>org.graalvm.buildtools</groupId>
    <artifactId>native-maven-plugin</artifactId>
    <version>0.10.x</version>
    <configuration>
        <imageName>meine-app</imageName>
        <mainClass>com.beispiel.Main</mainClass>
        <buildArgs>
            <buildArg>--no-fallback</buildArg>
            <buildArg>-O2</buildArg>
            <buildArg>--initialize-at-build-time=org.slf4j</buildArg>
        </buildArgs>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>compile-no-fork</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

```bash
# Native Image bauen
mvn -Pnative package

# Ausfuehren (kein java-Befehl noetig!)
./target/meine-app
```

### 3.4 Reflexions-Konfiguration

Native Image analysiert den Code statisch. Dynamische Reflexion muss konfiguriert werden.

```json
// src/main/resources/META-INF/native-image/reflect-config.json
[
  {
    "name": "com.beispiel.MeineKlasse",
    "allDeclaredConstructors": true,
    "allDeclaredMethods": true,
    "allDeclaredFields": true
  },
  {
    "name": "com.beispiel.DtoKlasse",
    "allPublicConstructors": true,
    "allPublicMethods": true
  }
]
```

```json
// resource-config.json – Ressourcen im nativen Image
{
  "resources": {
    "includes": [
      {"pattern": ".*\\.properties$"},
      {"pattern": ".*\\.xml$"},
      {"pattern": "META-INF/.*"}
    ]
  }
}
```

### 3.5 Tracing Agent – automatische Konfiguration generieren

```bash
# Anwendung mit Tracing Agent ausfuehren
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image \
     -jar meine-app.jar

# Agent beobachtet alle Reflexion-, Ressourcen-, JNI-Aufrufe
# und generiert Konfig-Dateien automatisch
```

### 3.6 Einfaches Native-Image-Beispiel

```java
package com.beispiel;

public class NativeBegruessung {

    public static void main(String[] args) {
        String name = args.length > 0 ? args[0] : "Welt";
        System.out.println("Hallo, " + name + "! (Native Image)");
        System.out.println("Max Memory: " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + " MB");
        System.out.println("Prozessoren: " + Runtime.getRuntime().availableProcessors());

        // Startzeit messen
        long start = System.nanoTime();
        long end   = System.nanoTime();
        System.out.printf("Startzeit-Messung: %.3f ms%n",
            (end - start) / 1_000_000.0);
    }
}
```

---

## 4. Helidon Framework

### 4.1 Helidon SE vs. Helidon MP

| Eigenschaft            | Helidon SE                        | Helidon MP                              |
|------------------------|-----------------------------------|-----------------------------------------|
| Programmiermodell      | Reaktiv / Funktional              | MicroProfile / CDI / JAX-RS             |
| Lernkurve              | Flach (kein Framework-Wissen)     | Steil (JEE-Kenntnisse hilfreich)        |
| Performance            | Sehr hoch                         | Hoch                                    |
| Native Image Support   | Excellent                         | Gut                                     |
| Wann verwenden         | Performance-kritisch, Microservice| Enterprise-Migrationen, MicroProfile    |

### 4.2 Helidon SE – Microservice

```java
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;

public class HelidonSeBeispiel {

    public static void main(String[] args) {
        WebServer server = WebServer.builder()
            .port(8080)
            .routing(HelidonSeBeispiel::routing)
            .build()
            .start();

        System.out.println("Server gestartet auf Port " + server.port());
    }

    static void routing(HttpRouting.Builder routing) {
        routing
            .get("/hallo", (req, res) ->
                res.send("Hallo von Helidon SE!"))

            .get("/hallo/{name}", (req, res) -> {
                String name = req.path().pathParameters().get("name");
                res.send("Hallo, " + name + "!");
            })

            .get("/gesundheit", (req, res) ->
                res.send("{\"status\":\"UP\"}"))

            .post("/echo", (req, res) -> {
                String koerper = req.content().as(String.class);
                res.send("Echo: " + koerper);
            });
    }
}
```

### 4.3 Helidon MP – MicroProfile

```java
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/api/produkte")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProduktResource {

    @Inject
    @ConfigProperty(name = "app.name", defaultValue = "Helidon App")
    private String appName;

    @GET
    public Response alleProdukte() {
        return Response.ok("""
            [
              {"id": 1, "name": "Kaffee", "preis": 3.99},
              {"id": 2, "name": "Tee",    "preis": 2.49}
            ]
            """).build();
    }

    @GET
    @Path("/{id}")
    public Response produktById(@PathParam("id") int id) {
        if (id == 1) {
            return Response.ok("""
                {"id": 1, "name": "Kaffee", "preis": 3.99}
                """).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/info")
    public String appInfo() {
        return "App: " + appName;
    }
}
```

### 4.4 Helidon pom.xml (BOM)

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.helidon</groupId>
            <artifactId>helidon-bom</artifactId>
            <version>4.x.x</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Helidon SE WebServer -->
    <dependency>
        <groupId>io.helidon.webserver</groupId>
        <artifactId>helidon-webserver</artifactId>
    </dependency>
    <!-- JSON-B fuer JSON-Serialisierung -->
    <dependency>
        <groupId>io.helidon.http.media</groupId>
        <artifactId>helidon-http-media-jsonb</artifactId>
    </dependency>
    <!-- Health Check -->
    <dependency>
        <groupId>io.helidon.webserver.observe</groupId>
        <artifactId>helidon-webserver-observe-health</artifactId>
    </dependency>
</dependencies>
```

---

## 5. Micronaut – AOT-optimiertes Framework

Micronaut fuehrt Dependency Injection zur Build-Zeit durch (kein Reflection zur Laufzeit), was es ideal fuer Native Image macht.

```java
import io.micronaut.http.annotation.*;
import io.micronaut.http.HttpResponse;
import jakarta.inject.Inject;

@Controller("/api")
public class ApiController {

    @Inject
    private ProduktService service;

    @Get("/hallo/{name}")
    public HttpResponse<String> begruessung(String name) {
        return HttpResponse.ok("Hallo, " + name + "!");
    }

    @Get("/produkte")
    public HttpResponse<?> alleProdukte() {
        return HttpResponse.ok(service.findAll());
    }
}

// Micronaut Application
import io.micronaut.runtime.Micronaut;

public class Application {
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
```

```xml
<!-- Micronaut pom.xml -->
<parent>
    <groupId>io.micronaut.platform</groupId>
    <artifactId>micronaut-parent</artifactId>
    <version>4.x.x</version>
</parent>
```

---

## 6. Containerisierung mit Docker

### 6.1 Dockerfile fuer JVM-Anwendungen

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# Stage 2: Runtime (schlankes JRE-Image)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user fuer Sicherheit
RUN addgroup -S javagruppe && adduser -S javauser -G javagruppe
USER javauser

COPY --from=build /app/target/meine-app.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
```

### 6.2 Dockerfile fuer Native Image

```dockerfile
# Stage 1: Native Image bauen
FROM ghcr.io/graalvm/native-image:ol9-java21 AS nativebuild
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -Pnative package -DskipTests -q

# Stage 2: Minimales Runtime-Image
FROM oraclelinux:9-slim
WORKDIR /app

RUN adduser -r javauser
USER javauser

COPY --from=nativebuild /app/target/meine-app .

EXPOSE 8080
ENTRYPOINT ["./meine-app"]
```

### 6.3 Docker-Befehle

```bash
# Image bauen
docker build -t meine-java-app:1.0 .

# Container starten
docker run -d \
  --name meine-app \
  -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://db:5432/prod \
  -e DB_USER=app \
  --memory="512m" \
  --cpus="1.0" \
  meine-java-app:1.0

# Logs anzeigen
docker logs -f meine-app

# In Container einsteigen
docker exec -it meine-app /bin/sh

# Image nach OCI Container Registry pushen
docker tag meine-java-app:1.0 \
    <region>.ocir.io/<tenancy>/<repo>/meine-java-app:1.0
docker push <region>.ocir.io/<tenancy>/<repo>/meine-java-app:1.0
```

### 6.4 Groessenvergleich

| Image-Typ                  | Basisimage            | Typische Groesse |
|----------------------------|-----------------------|------------------|
| JVM (Full JDK)             | openjdk:21            | ~400 MB          |
| JVM (JRE)                  | eclipse-temurin:21-jre | ~220 MB         |
| JVM (Alpine JRE)           | eclipse-temurin:21-jre-alpine | ~100 MB |
| Native Image               | scratch / distroless  | 15 – 50 MB       |

---

## 7. OCI Deployment

### 7.1 OCI Container Registry (OCIR)

```bash
# Bei OCI Container Registry anmelden
docker login <region>.ocir.io \
  --username "<tenancy-namespace>/<iam-username>" \
  --password "<auth-token>"

# Image taggen
docker tag meine-app:1.0 \
    fra.ocir.io/meintenancy/mein-repo/meine-app:1.0

# Image pushen
docker push fra.ocir.io/meintenancy/mein-repo/meine-app:1.0
```

### 7.2 OCI Container Instances

```bash
# Container Instance erstellen (OCI CLI)
oci container-instances container-instance create \
  --compartment-id ocid1.compartment.oc1.. \
  --availability-domain "AD-1" \
  --shape CI.Standard.E4.Flex \
  --shape-config '{"ocpus": 1.0, "memoryInGBs": 4.0}' \
  --containers '[{
    "image": "fra.ocir.io/meintenancy/mein-repo/meine-app:1.0",
    "displayName": "meine-app-container",
    "environmentVariables": {
      "APP_ENV": "production",
      "PORT": "8080"
    },
    "resourceConfig": {
      "memoryLimitInGBs": 2.0,
      "vcpusLimit": 0.5
    }
  }]' \
  --vnics '[{
    "subnetId": "ocid1.subnet.oc1..",
    "isPublicIpAssigned": true
  }]'
```

### 7.3 OCI Functions (Serverless)

```java
package com.beispiel.fn;

import com.fnproject.fn.api.FnConfiguration;
import com.fnproject.fn.api.RuntimeContext;

public class HelloFunction {

    @FnConfiguration
    public void konfigurieren(RuntimeContext ctx) {
        ctx.getAttribute("DB_URL", String.class)
            .ifPresent(url -> System.out.println("DB: " + url));
    }

    public String handleRequest(String input) {
        String name = (input == null || input.isBlank()) ? "Welt" : input;
        return "Hallo von OCI Function, " + name + "!";
    }
}
```

```yaml
# func.yaml
schema_version: 20180708
name: hello-function
version: 0.0.1
runtime: java21
build_image: fnproject/fn-java-fdk-build:jdk21-1.0.x
run_image: fnproject/fn-java-fdk:jdk21-1.0.x
cmd: com.beispiel.fn.HelloFunction::handleRequest
```

```bash
# Function deployen
fn deploy --app meine-fn-app

# Function aufrufen
fn invoke meine-fn-app hello-function
echo "Java" | fn invoke meine-fn-app hello-function
```

### 7.4 Kubernetes Deployment (OKE)

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: meine-java-app
  namespace: default
spec:
  replicas: 3
  selector:
    matchLabels:
      app: meine-java-app
  template:
    metadata:
      labels:
        app: meine-java-app
    spec:
      containers:
      - name: app
        image: fra.ocir.io/meintenancy/mein-repo/meine-app:1.0
        ports:
        - containerPort: 8080
        env:
        - name: DB_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /health/live
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health/ready
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: meine-java-app-svc
spec:
  selector:
    app: meine-java-app
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

---

## 8. GraalVM Polyglot API

```java
import org.graalvm.polyglot.*;

public class PolyglotBeispiel {

    public static void main(String[] args) {
        // JavaScript in Java ausfuehren
        try (Context context = Context.create()) {
            // Einfache Auswertung
            int ergebnis = context.eval("js", "1 + 2 + 3").asInt();
            System.out.println("JS Ergebnis: " + ergebnis); // 6

            // Java-Objekt an JS uebergeben
            context.getBindings("js").putMember("javaMessage", "Hallo von Java!");
            String antwort = context.eval("js",
                "'JS sagt: ' + javaMessage.toUpperCase()").asString();
            System.out.println(antwort); // JS sagt: HALLO VON JAVA!

            // Arrays verarbeiten
            Value jsArray = context.eval("js", "[1, 2, 3, 4, 5].map(x => x * x)");
            for (int i = 0; i < jsArray.getArraySize(); i++) {
                System.out.print(jsArray.getArrayElement(i) + " "); // 1 4 9 16 25
            }
        }
    }
}
```

---

## 9. Vollstaendiges Cloud-native Beispiel

```java
package com.beispiel.cloud;

import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Einfacher Cloud-nativer Produkt-Microservice mit Helidon SE. */
public class ProduktMicroservice {

    record Produkt(int id, String name, double preis) {}

    private static final Map<Integer, Produkt> STORE = new ConcurrentHashMap<>();
    private static final AtomicInteger ID_GEN = new AtomicInteger(1);

    static {
        STORE.put(1, new Produkt(1, "Kaffee", 3.99));
        STORE.put(2, new Produkt(2, "Tee",    2.49));
    }

    public static void main(String[] args) {
        WebServer server = WebServer.builder()
            .port(8080)
            .routing(ProduktMicroservice::routing)
            .build()
            .start();

        System.out.println("Microservice gestartet: http://localhost:" + server.port());
        System.out.println("Health: http://localhost:" + server.port() + "/health");
    }

    static void routing(HttpRouting.Builder r) {
        r
            // Healthcheck
            .get("/health", (req, res) ->
                res.send("{\"status\":\"UP\",\"service\":\"ProduktMicroservice\"}"))

            // Alle Produkte
            .get("/api/produkte", ProduktMicroservice::alleProdukte)

            // Produkt nach ID
            .get("/api/produkte/{id}", ProduktMicroservice::produktById)

            // Neues Produkt
            .post("/api/produkte", ProduktMicroservice::neuesProdukt)

            // Produkt loeschen
            .delete("/api/produkte/{id}", ProduktMicroservice::produktLoeschen);
    }

    static void alleProdukte(ServerRequest req, ServerResponse res) {
        List<Produkt> liste = List.copyOf(STORE.values());
        res.headers().add("Content-Type", "application/json");
        res.send(produktListeZuJson(liste));
    }

    static void produktById(ServerRequest req, ServerResponse res) {
        int id = Integer.parseInt(req.path().pathParameters().get("id"));
        Produkt produkt = STORE.get(id);
        if (produkt == null) {
            res.status(404).send("{\"fehler\":\"Produkt nicht gefunden\"}");
        } else {
            res.headers().add("Content-Type", "application/json");
            res.send(produktZuJson(produkt));
        }
    }

    static void neuesProdukt(ServerRequest req, ServerResponse res) {
        // Vereinfacht: Name und Preis aus Query-Parametern
        String name  = req.query().get("name");
        double preis = Double.parseDouble(req.query().get("preis", "0.0"));
        int newId    = ID_GEN.getAndIncrement();
        Produkt neu  = new Produkt(newId, name, preis);
        STORE.put(newId, neu);
        res.status(201)
           .headers().add("Content-Type", "application/json");
        res.send(produktZuJson(neu));
    }

    static void produktLoeschen(ServerRequest req, ServerResponse res) {
        int id = Integer.parseInt(req.path().pathParameters().get("id"));
        if (STORE.remove(id) != null) {
            res.status(204).send();
        } else {
            res.status(404).send("{\"fehler\":\"Produkt nicht gefunden\"}");
        }
    }

    private static String produktZuJson(Produkt p) {
        return "{\"id\":%d,\"name\":\"%s\",\"preis\":%.2f}"
            .formatted(p.id(), p.name(), p.preis());
    }

    private static String produktListeZuJson(List<Produkt> liste) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < liste.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(produktZuJson(liste.get(i)));
        }
        return sb.append("]").toString();
    }
}
```

---

## 10. Vergleich: Frameworks fuer Native Image

| Framework      | Native Image | Kaltstart     | Speicher | GraalVM noetig | Lernkurve |
|----------------|-------------|---------------|----------|-----------------|-----------|
| Helidon SE     | Sehr gut     | ~30ms         | ~20 MB   | Empfohlen       | Niedrig   |
| Helidon MP     | Gut          | ~100ms        | ~40 MB   | Empfohlen       | Mittel    |
| Micronaut      | Sehr gut     | ~25ms         | ~20 MB   | Empfohlen       | Mittel    |
| Quarkus        | Sehr gut     | ~20ms         | ~15 MB   | Empfohlen       | Mittel    |
| Spring Boot 3  | Gut          | ~100ms        | ~50 MB   | Erforderlich    | Niedrig   |
| Spring Boot 3 (JVM) | –       | ~2s           | ~150 MB  | Nein            | Niedrig   |

---

## Zusammenfassung

- **OCI**: Globale Cloud-Plattform mit Compute, OKE (Kubernetes), Functions und Container Instances.
- **Oracle JDK vs. GraalVM**: GraalVM erhaelt Java-Unterstuetzung plus Native Image und Polyglot.
- **Native Image AOT**: Schneller Kaltstart (~50ms), geringer Speicher; Reflexion per JSON-Config konfigurieren.
- **Helidon SE**: Reaktiver Microservice-Framework; ideal mit Native Image; kein Framework-Overhead.
- **Helidon MP**: MicroProfile-konform; CDI + JAX-RS; fuer Enterprise-Migrationen geeignet.
- **Micronaut**: AOT-Dependency-Injection; kein Reflection zur Laufzeit; sehr Native-Image-freundlich.
- **Docker**: Multistage-Builds; Non-root-User; JVM-Container ~100 MB, Native-Image-Container ~20 MB.
- **OCI Deployment**: OCIR als Registry; Container Instances fuer einfache Deployments; OKE fuer Kubernetes.

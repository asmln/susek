plugins {
	java
	id("org.springframework.boot") version "4.2.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("io.openapiprocessor.openapi-processor") version "2026.2"
}

group = "com.asmln"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

openapiProcessor {
    apiPath(layout.projectDirectory.file("doc/susek.openapi.yaml").asFile.path)
    process("spring") {
        processor("io.openapiprocessor:openapi-processor-spring:2026.3")
        targetDir(layout.buildDirectory.dir("generated/openapi").get().asFile.absolutePath)
        prop(
            "mapping",
            layout.projectDirectory.file("src/main/resources/openapi/mapping.yaml").asFile.absolutePath
        )
    }
}

sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated/openapi"))
        }
    }
}

tasks.named("compileJava") {
    dependsOn(tasks.named("processSpring"))
}

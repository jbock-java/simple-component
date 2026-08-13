plugins {
  id("java")
  id("com.vanniktech.maven.publish") version "0.37.0"
}

group = "io.github.jbock-java"

tasks.withType<JavaCompile>().configureEach {
  options.encoding = "UTF-8"
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Javadoc>().configureEach {
  options.encoding = "UTF-8"
  (options as CoreJavadocOptions).addBooleanOption("Xdoclint:none", true)
}

repositories {
  mavenCentral()
}

dependencies {
  var simple_component = project(":simple-component")
  implementation("com.palantir.javapoet:javapoet:0.18.0")
  implementation("io.github.jbock-java:auto-common:1.2.3")
  implementation(simple_component)
  annotationProcessor("io.github.jbock-java:simple-component-compiler:1.026")
  testImplementation("io.github.jbock-java:compile-testing:0.19.12")
  testImplementation(platform("org.junit:junit-bom:6.1.2"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation(simple_component)
  testImplementation("jakarta.inject:jakarta.inject-api:2.0.1")
  testImplementation("javax.inject:javax.inject:1")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


tasks.withType<Jar> {
  manifest {
    attributes["Implementation-Version"] = project.version
  }
}

tasks.named<Test>("test") {
  useJUnitPlatform()
}

// https://vanniktech.github.io/gradle-maven-publish-plugin/central/
mavenPublishing {
  coordinates("io.github.jbock-java", "simple-component-compiler", project.version?.toString())

  pom {
    name = "simple-component-compiler"
    packaging = "jar"
    description = "simple-component"
    url = "https://github.com/jbock-java/simple-component"

    licenses {
      license {
        name = "MIT License"
        url = "https://opensource.org/licenses/MIT"
      }
    }
    developers {
      developer {
        id = "Various"
        name = "Various"
        email = "jbock-java@gmx.de"
      }
    }
    scm {
      connection = "scm:git:https://github.com/jbock-java/simple-component.git"
      developerConnection = "scm:git:https://github.com/jbock-java/simple-component.git"
      url = "https://github.com/jbock-java/simple-component"
    }
  }
  publishToMavenCentral()
  signAllPublications()
}

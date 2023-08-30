import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.9.10"
}

allprojects {
	apply(plugin = "kotlin")

	group = "fr.smo"
	version = "0.0.1-SNAPSHOT"

	tasks.withType<KotlinCompile> {
		kotlinOptions {
			jvmTarget = "17"
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}

	dependencies {
		val junitVersion = "5.10.0"
		val striktVersion = "0.34.1"
		implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
		implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
		testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
		testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
		testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
		testRuntimeOnly("org.junit.jupiter:junit-jupiter-params:$junitVersion")
		testImplementation("io.strikt:strikt-core:$striktVersion")
	}

	repositories {
		mavenCentral()
	}

}
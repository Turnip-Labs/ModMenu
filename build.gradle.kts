@file:Suppress("UnstableApiUsage", "PropertyName")

import org.apache.tools.ant.taskdefs.condition.Os
import java.io.FileNotFoundException
import java.lang.RuntimeException
import java.net.URL

plugins {
	id("maven-publish")
	id("fabric-loom") version "1.10.0-bta"
	id("java")
}

val lwjglVersion = "3.3.4"

val lwjglNatives = when {
	Os.isFamily(Os.FAMILY_UNIX) && !Os.isFamily(Os.FAMILY_MAC) -> "natives-linux"
	Os.isFamily(Os.FAMILY_WINDOWS) -> "natives-windows"
	Os.isFamily(Os.FAMILY_MAC) -> "natives-macos${if (Os.isArch("aarch64")) "-arm64" else ""}"
	else -> error("Unsupported OS")
}

val mod_group: String by project
val mod_name: String by project
val mod_version: String by project

val bta_channel: String by project
val bta_version: String by project

val loader_version: String by project

group = mod_group
base.archivesName.set(mod_name)
version = mod_version

loom {
	noIntermediateMappings()
	customMinecraftMetadata.set("https://downloads.betterthanadventure.net/bta-client/$bta_channel/$bta_version/manifest.json")
}

repositories {
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
	maven {
		name = "Babric"
		url = uri("https://maven.glass-launcher.net/babric")
	}
	maven {
		name = "Fabric"
		url = uri("https://maven.fabricmc.net/")
	}
	maven {
		name = "SignalumMavenInfrastructure"
		url = uri("https://maven.thesignalumproject.net/infrastructure")
	}
	maven {
		name = "SignalumMavenReleases"
		url = uri("https://maven.thesignalumproject.net/releases")
	}
	maven {
		name = "SignalumMavenNightly"
		url = uri("https://maven.thesignalumproject.net/nightly")
	}
	ivy {
		url = uri("https://github.com/Better-than-Adventure")
		patternLayout {
			artifact("[organisation]/releases/download/v[revision]/[module].jar")
		}
		metadataSources { artifact() }
	}
	ivy {
		url = uri("https://downloads.betterthanadventure.net/bta-client/$bta_channel/")
		patternLayout {
			artifact("/v[revision]/client.jar")
		}
		metadataSources { artifact() }
	}
	ivy {
		url = uri("https://downloads.betterthanadventure.net/bta-server/$bta_channel/")
		patternLayout {
			artifact("/v[revision]/server.jar")
		}
		metadataSources { artifact() }
	}
	ivy {
		url = uri("https://piston-data.mojang.com")
		patternLayout {
			artifact("v1/[organisation]/[revision]/[module].jar")
		}
		metadataSources { artifact() }
	}
}

dependencies {
	minecraft("::${bta_version}")
	mappings(loom.layered {})

	modRuntimeOnly("objects:client:43db9b498cb67058d2e12d394e6507722e71bb45") // https://piston-data.mojang.com/v1/objects/43db9b498cb67058d2e12d394e6507722e71bb45/client.jar
	modImplementation("net.fabricmc:fabric-loader:$loader_version")

	implementation("org.slf4j:slf4j-api:1.8.0-beta4")
	implementation("org.apache.logging.log4j:log4j-slf4j18-impl:2.16.0")

	implementation("com.google.guava:guava:33.0.0-jre")
	implementation("com.google.code.gson:gson:2.10.1")

	val log4jVersion = "2.20.0"
	implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
	implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
	implementation("org.apache.logging.log4j:log4j-1.2-api:$log4jVersion")

	implementation("org.apache.commons:commons-lang3:3.12.0")
	include("org.apache.commons:commons-lang3:3.12.0")

	modImplementation("com.github.Better-than-Adventure:legacy-lwjgl3:1.0.5")
	implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

	runtimeOnly("org.lwjgl:lwjgl::$lwjglNatives")
	runtimeOnly("org.lwjgl:lwjgl-assimp::$lwjglNatives")
	runtimeOnly("org.lwjgl:lwjgl-glfw::$lwjglNatives")
	runtimeOnly("org.lwjgl:lwjgl-openal::$lwjglNatives")
	runtimeOnly("org.lwjgl:lwjgl-opengl::$lwjglNatives")
	runtimeOnly("org.lwjgl:lwjgl-stb::$lwjglNatives")
	implementation("org.lwjgl:lwjgl:$lwjglVersion")
	implementation("org.lwjgl:lwjgl-assimp:$lwjglVersion")
	implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
	implementation("org.lwjgl:lwjgl-openal:$lwjglVersion")
	implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")
	implementation("org.lwjgl:lwjgl-stb:$lwjglVersion")
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
	withSourcesJar()
}

tasks.compileJava {
	options.release.set(8)
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${base.archivesName.get()}" }
	}
}

configurations.configureEach {
	// Removes LWJGL2 dependencies
	exclude(group = "org.lwjgl.lwjgl")
}

tasks.processResources {
	inputs.property("version", version)
	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

/////////////////// Signalum publishing ///////////////////

val publishGroup = mod_group
val publishName = mod_name
val publishVersion = mod_version

val signalumName = "signalumMavenNightly"
val signalumNamespace = "nightly"

fun checkSignalumReleaseStatus(groupId: String, modName: String, version: String) {
	val url: URL = URL("https://maven.thesignalumproject.net/$signalumNamespace/$groupId/$modName/maven-metadata.xml")
	val xmlString = try {
		url.readText(Charsets.UTF_8)
	} catch (e: FileNotFoundException) {
		""
	}

	logger.info("metadata url: {}", url)
	//This is probably awful, but I can't for the life of me find a proper xml parser
	if (xmlString.contains(version)) throw RuntimeException("Version '$publishVersion' already published.\nURL: $url")
}

val versionCheckTask: Task = task("checkSignalum").doFirst {
	checkSignalumReleaseStatus(publishGroup, publishName, publishVersion)
}

val publishTask = tasks.publish.get()
publishTask.mustRunAfter(versionCheckTask)

publishing {
	repositories.maven {
		name = signalumName
		url = uri("https://maven.thesignalumproject.net/$signalumNamespace")
		credentials(PasswordCredentials::class)
		authentication.create<BasicAuthentication>("basic")
	}

	publications.create<MavenPublication>("maven") {
		groupId = publishGroup
		artifactId = publishName
		version = publishVersion
		from(components.getByName("java"))
	}
}

/////////////////// Signalum publishing ///////////////////
## Depending on Dashpanels

### build.gradle

```groovy
    repositories {
        maven {
            url = "https://api.modrinth.com/maven"
        }
    }

    dependencies {
        implementation("maven.modrinth:dashpanels:2.0+neoforge1.21.1")
    }
```
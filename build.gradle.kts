// Kotlin konfigurasjonen er gitt av pluginen 'aap.conventions' i buildSrc
// og settings.gradle.kts

plugins {
    base
    id("aap.conventions")
}

// Call the tasks of the subprojects
for (taskName in listOf<String>("clean", "build", "assemble", "check")) {
    tasks.named(taskName) {
        dependsOn(subprojects.map { it.tasks.named(taskName) })
    }
}

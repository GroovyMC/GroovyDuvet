
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.DslContext
import jetbrains.buildServer.configs.kotlin.buildFeatures.Swabra
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.swabra
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.project
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.ui.add
import jetbrains.buildServer.configs.kotlin.version

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.
VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.
To debug settings scripts in command-line, run the
    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate
command and attach your debugger to the port 8000.
To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2022.10"

project {
    buildType(GroovyMC_groovyduvet_Build)
}

object GroovyMC_groovyduvet_Build : BuildType({
    id("Build")
    name = "Build"

    vcs {
        root(DslContext.settingsRoot)
    }

    triggers {
        vcs {
            triggerRules = "-:comment=\\[noci]:**"
        }
    }

    features {
        add {
            swabra {
                filesCleanup = Swabra.FilesCleanup.AFTER_BUILD
                lockingProcesses = Swabra.LockingProcessPolicy.KILL
            }
        }
        add {
            commitStatusPublisher {
                publisher = github {
                    githubUrl = "https://api.github.com"
                    authType = personalToken {
                        token = "%commit_status_publisher%"
                    }
                }
            }
        }
    }

    steps {
        /*gradle {
            jvmArgs = "-Xmx3G"
            workingDir = "main"
            useGradleWrapper = true
            name = "Configure TeamCity information"
            tasks = "configureTeamCity"
        }

        gradle {
            jvmArgs = "-Xmx3G"
            workingDir = "main"
            useGradleWrapper = true
            name = "Clean build directory"
            tasks = "clean"
        }

        gradle {
            jvmArgs = "-Xmx3G"
            workingDir = "main"
            useGradleWrapper = true
            name = "Build Gradle Project"
            tasks = "build"
        }

        gradle {
            jvmArgs = "-Xmx3G"
            workingDir = "main"
            useGradleWrapper = true
            name = "Publish Gradle Project"
            tasks = "publish curseforge modrinth"
        }*/
    }
})

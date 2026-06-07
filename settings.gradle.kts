pluginManagement {
    repositories {
        // 阿里云镜像
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { setUrl("https://maven.aliyun.com/repository/central") }
        // 华为云镜像
        maven { setUrl("https://repo.huaweicloud.com/repository/maven/") }
        // 腾讯云镜像
        maven { setUrl("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        // 网易镜像
        maven { setUrl("https://mirrors.163.com/maven/repository/maven-public/") }
        // 首都在线
        maven { setUrl("https://maven.oscs.oschina.net/content/groups/public/") }

        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 阿里云镜像
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { setUrl("https://maven.aliyun.com/repository/central") }
        // 华为云镜像
        maven { setUrl("https://repo.huaweicloud.com/repository/maven/") }
        // 腾讯云镜像
        maven { setUrl("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        // 网易镜像
        maven { setUrl("https://mirrors.163.com/maven/repository/maven-public/") }
        // 首都在线
        maven { setUrl("https://maven.oscs.oschina.net/content/groups/public/") }

        google()
        mavenCentral()
    }
}

rootProject.name = "eink"
include(":app")

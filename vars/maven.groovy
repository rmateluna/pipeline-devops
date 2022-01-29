/*

    forma de invocación de método call:

    def ejecucion = load 'script.groovy'
    ejecucion.call()

*/

def call(){
    env.STAGE = "Paso 1: Compliar"
    stage("$env.STAGE "){
        sh "mvn clean compile -e"
    }
    env.STAGE = "Paso 2: Testear"
    stage("$env.STAGE "){
        sh "mvn clean test -e"
    }
    env.STAGE = "Paso 3: Build .Jar"
    stage("$env.STAGE "){
        sh "mvn clean package -e"
    }
    env.STAGE = "Paso 4: Análisis SonarQube"
    stage("$env.STAGE "){
        sh "echo 'Análisis Estático!'"
        withSonarQubeEnv('sonarqube') {
            sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=sonar-github -Dsonar.java.binaries=build'
        }
    }
    env.STAGE = "Paso 5: Curl Springboot Gradle sleep 60"
    stage("$env.STAGE "){
        sh "gradle bootRun&"
        sh "sleep 60 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
    env.STAGE = "Paso 6: Subir Nexus"
    stage("$env.STAGE "){
        nexusPublisher nexusInstanceId: 'nexus',
        nexusRepositoryId: 'devops-usach-nexus',
        packages: [
            [$class: 'MavenPackage',
                mavenAssetList: [
                    [classifier: '',
                    extension: '.jar',
                    filePath: 'build/DevOpsUsach2020-0.0.1.jar'
                ]
            ],
                mavenCoordinate: [
                    artifactId: 'DevOpsUsach2020',
                    groupId: 'com.devopsusach2020',
                    packaging: 'jar',
                    version: '0.0.1'
                ]
            ]
        ]
    }
    env.STAGE = "Paso 7: Descargar Nexu"
    stage("$env.STAGE "){
        sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
    }
    env.STAGE = "Paso 8: Levantar Artefacto Jar"
    stage("$env.STAGE "){
        sh 'nohup bash java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
    }
    env.STAGE = "Paso 9: Testear Artefacto - Dormir(Esperar 60sg"
    stage("$env.STAGE "){
        sh "sleep 60 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}

return this;
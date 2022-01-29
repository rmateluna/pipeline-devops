/*

    forma de invocación de método call:

    def ejecucion = load 'script.groovy'
    ejecucion.call()

*/

def call(){
    stage("Paso 1: Compliar"){
        sh "mvn clean compile -e"
    }
    stage("Paso 2: Testear"){
        sh "mvn clean test -e"
    }
    stage("Paso 3: Build .Jar"){
        sh "mvn clean package -e"
    }
    stage("Paso 4: Análisis SonarQube"){
        sh "echo 'Análisis Estático!'"
        withSonarQubeEnv('sonarqube') {
            sh 'mvn clean verify sonar:sonar -Dsonar.projectKey=sonar-github -Dsonar.java.binaries=build'
        }
    }
    stage("Paso 5: Curl Springboot Gradle sleep 60"){
        sh "gradle bootRun&"
        sh "sleep 60 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
    stage("Paso 6: Subir Nexus"){
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
    stage("Paso 7: Descargar Nexus"){
        sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
    }
    stage("Paso 8: Levantar Artefacto Jar"){
        sh 'nohup bash java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
    }
    stage("Paso 9: Testear Artefacto - Dormir(Esperar 60sg) "){
        sh "sleep 60 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}

return this;
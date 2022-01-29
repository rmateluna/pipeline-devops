/*

    forma de invocación de método call:

    def ejecucion = load 'script.groovy'
    ejecucion.call()

*/

def call(){
    pipeline {
        agent any
        environment {
            NEXUS_USER      = credentials('user-nexus')
            NEXUS_PASSWORD  = credentials('password-nexus')
        }
        parameters {
            choice  name: 'compileTool', choices: ['Gradle', 'Maven'], description: 'Seleccione el empaquetador maven/gradle'
            string  name: 'stages', description: 'Ingrese los stages para ejecutar', trim: true
        }
        stages {
            stage('pipeline') {
                steps {
                    script {
                        sh "env"
                        env.STAGE  = ""
                        switch(params.compileTool)
                        {
                            case 'Maven':
                                //def ejecucion = load 'maven.groovy'
                                //ejecucion.call()
                                maven.call()
                            break;
                            case 'Gradle':
                                //def ejecucion = load 'gradle.groovy'
                                //ejecucion.call()
                                gradle.call()
                            break;
                        }
                    }
                }
            }
        }
    }
}

return this;
pipeline { 
    agent any 

    tools { 
        maven 'Maven-3.6.3' 
        jdk 'jdk8' // On garde Java 8 
    } 

    environment { 
        GIT_REPO = 'https://github.com/mohamedaminesabehy/BackEndAFH.git' 
        BRANCH = 'main' 
        SONAR_TOKEN = 'b5e58eed69e2c57f71bbfc8d4d47fcc5b17fc119' 
        SONAR_HOST = 'http://localhost:9000' 
        SONAR_PROJECT_KEY = 'stage_project' 
        SONAR_PLUGIN_VERSION = '3.9.1.2184' // Version compatible Java 8 
    } 

    stages { 

        stage('Checkout') { 
            steps { 
                git branch: "${BRANCH}", url: "${GIT_REPO}" 
            } 
        } 
        
        stage('Prepare JaCoCo Agent') {
            steps {
                sh 'mvn clean jacoco:prepare-agent'
            }
        }

        stage('Unit Tests') { 
            steps { 
                sh 'mvn test' 
            } 
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    jacoco(
                        execPattern: 'target/jacoco.exec',
                        classPattern: 'target/classes',
                        sourcePattern: 'src/main/java',
                        exclusionPattern: '**/model/**,**/dto/**,**/config/**'
                    )
                }
            }
        } 
        
        stage('Code Quality Analysis') {
            steps {
                sh 'mvn pmd:pmd pmd:cpd'
            }
        }
        
        stage('Security Analysis') {
            steps {
                sh 'mvn dependency-check:check'
            }
        }

        stage('Build & Package') { 
            steps { 
                sh 'mvn package -DskipTests' // Tests déjà exécutés 
            } 
        } 

        stage('SonarQube Analysis') { 
            steps { 
                sh """ 
                    mvn org.sonarsource.scanner.maven:sonar-maven-plugin:${SONAR_PLUGIN_VERSION}:sonar \
                        -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                        -Dsonar.host.url=${SONAR_HOST} \
                        -Dsonar.login=${SONAR_TOKEN} \
                        -Dsonar.java.coveragePlugin=jacoco \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                        -Dsonar.coverage.exclusions=**/model/**/*.java,**/dto/**/*.java,**/config/**/*.java \
                        -Dsonar.cpd.exclusions=**/model/**/*.java,**/dto/**/*.java \
                        -Dsonar.qualitygate.wait=true
                """ 
            } 
        } 

        stage('Archive Artifact') { 
            steps { 
                archiveArtifacts artifacts: 'target/GESCOMP.war', fingerprint: true 
            } 
        } 

        stage('Deploy to Tomcat') { 
            when { 
                expression { return fileExists('target/GESCOMP.war') } 
            } 
            steps { 
                sh ''' 
                    cp target/GESCOMP.war /opt/tomcat9/webapps/ 
                    echo "✅ Deployed GESCOMP.war to Tomcat" 
                ''' 
            } 
        } 

    } 

    post { 
        success { 
            echo '✅ Build SUCCESSFUL!' 
        } 
        failure { 
            echo '❌ Build FAILED!' 
        } 
        always {
            publishHTML([
                allowMissing: false,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'JaCoCo Coverage Report'
            ])
            
            publishHTML([
                allowMissing: false,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/dependency-check-report',
                reportFiles: 'dependency-check-report.html',
                reportName: 'Dependency Check Report'
            ])
        }
    } 
}
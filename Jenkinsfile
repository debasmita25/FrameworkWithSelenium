pipeline {
    agent any

    tools {
        maven 'Maven'   // Name from Jenkins Global Tool Configuration
        jdk 'JDK'       // Java version installed in Jenkins
    }

    environment {
        REPORT_HTML = "target\\reports\\extent-report.html"
        SCREENSHOT_BASE_DIR = "target\\screenshots"
        LOG_DIR = "logs"
        REPORT_NAME = "Test Execution Report"
        EMAIL_TO = "your_email@example.com" // 🔁 Replace this
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Run Tests') {
            steps {
                bat "mvn clean test -Dheadless=true"
            }
        }

        stage('Zip Artifacts') {
            steps {
                script {
                    // Find latest screenshot timestamp folder
                    def latestScreenshotFolder = powershell(returnStdout: true, script: """
                        Get-ChildItem -Directory "${env.SCREENSHOT_BASE_DIR}" |
                        Sort-Object LastWriteTime -Descending |
                        Select-Object -First 1 |
                        Select-Object -ExpandProperty Name
                    """).trim()

                    echo "Latest Screenshot Folder: ${latestScreenshotFolder}"

                    def screenshotFullPath = "${env.SCREENSHOT_BASE_DIR}\\${latestScreenshotFolder}"
                    def screenshotZip = "screenshots.zip"
                    def logsZip = "logs.zip"

                    // Zip screenshots
                    bat "powershell Compress-Archive -Path \"${screenshotFullPath}\\*\" -DestinationPath ${screenshotZip}"

                    // Zip logs
                    bat "powershell Compress-Archive -Path \"${env.LOG_DIR}\\*\" -DestinationPath ${logsZip}"

                    // Set environment for attachments
                    env.SCREENSHOT_ZIP = screenshotZip
                    env.LOG_ZIP = logsZip
                }
            }
        }

        stage('Publish HTML Report') {
            steps {
                publishHTML(target: [
                    reportDir: 'target/reports',
                    reportFiles: 'extent-report.html',
                    reportName: "${env.REPORT_NAME}",
                    alwaysLinkToLastBuild: true,
                    keepAll: true
                ])
            }
        }

        stage('Email with Report') {
            steps {
                emailext(
                    subject: "Test Report - Build #${env.BUILD_NUMBER}",
                    body: """
                        <p>Hello Team,</p>
                        <p>Test execution completed. Please find below details:</p>
                        <ul>
                            <li><a href="${env.BUILD_URL}HTML_20Report/">Click here to view Extent Report</a></li>
                        </ul>
                        <p>Regards,<br>Automation Team</p>
                    """,
                    mimeType: 'text/html',
                    to: "${env.EMAIL_TO}",
                    attachmentsPattern: "${env.REPORT_HTML},${env.SCREENSHOT_ZIP},${env.LOG_ZIP}"
                )
            }
        }
    }

    post {
        always {
            echo "Pipeline execution completed."
        }
    }
}

pipeline {
    agent any

    tools {
        maven 'Maven-3.9.10'
        jdk 'Java 17'
    }

    environment {
        REPORT_DIR = "target\\reports"
        REPORT_HTML = "target\\reports\\extent-report.html"
        GENERATED_REPORT_PATH = "some\\custom\\output\\location\\extent-report.html" // change if needed
        SCREENSHOT_BASE_DIR = "target\\screenshots"
        LOG_DIR = "logs"
        REPORT_NAME = "Test Execution Report"
        EMAIL_TO = "debasmita25@gmail.com"
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

        stage('Ensure HTML Report in Workspace') {
            steps {
                script {
                    // Create reports directory if it doesn't exist
                    bat """if not exist "${env.REPORT_DIR}" mkdir "${env.REPORT_DIR}" """

                    // If report is generated elsewhere, copy it to target/reports
                    if (env.GENERATED_REPORT_PATH != env.REPORT_HTML) {
                        bat """if exist "${env.GENERATED_REPORT_PATH}" copy "${env.GENERATED_REPORT_PATH}" "${env.REPORT_HTML}" /Y"""
                    }
                }
            }
        }

        stage('Zip Artifacts') {
            steps {
                script {
                    env.SCREENSHOT_ZIP = ''
                    env.LOG_ZIP = ''
                    def screenshotZip = "screenshots.zip"
                    def logsZip = "logs.zip"

                    // Zip latest screenshot folder if exists
                    def latestScreenshotFolder = powershell(returnStdout: true, script: """
                        if (Test-Path '${env.SCREENSHOT_BASE_DIR}') {
                            Get-ChildItem -Directory '${env.SCREENSHOT_BASE_DIR}' |
                            Sort-Object LastWriteTime -Descending |
                            Select-Object -First 1 |
                            Select-Object -ExpandProperty Name
                        }
                    """).trim()

                    if (latestScreenshotFolder) {
                        echo "📸 Latest Screenshot Folder: ${latestScreenshotFolder}"
                        def screenshotFullPath = "${env.SCREENSHOT_BASE_DIR}\\${latestScreenshotFolder}"
                        bat """powershell -Command "if (Test-Path '${screenshotFullPath}') {
                            Compress-Archive -Path '${screenshotFullPath}\\*' -DestinationPath '${screenshotZip}' -Force
                        }" """
                        env.SCREENSHOT_ZIP = screenshotZip
                    } else {
                        echo "⚠️ No screenshots folder found. Skipping zip."
                    }

                    // Zip logs if available
                    def logCheck = powershell(returnStatus: true, script: "Test-Path '${env.LOG_DIR}'")
                    if (logCheck == 0) {
                        bat """powershell -Command "Compress-Archive -Path '${env.LOG_DIR}\\*' -DestinationPath '${logsZip}' -Force" """
                        env.LOG_ZIP = logsZip
                    } else {
                        echo "⚠️ Log folder not found. Skipping zip."
                    }
                }
            }
        }

        stage('Publish HTML Report') {
            steps {
                publishHTML(target: [
                    reportDir: 'target/reports',
                    reportFiles: 'extent-report.html',
                    reportName: "${env.REPORT_NAME}",
                    reportTitles: 'Test Results',
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true
                ])
            }
        }

        stage('Verify Report Exists') {
            steps {
                script {
                    if (fileExists(env.REPORT_HTML)) {
                        echo "✅ Report found: ${env.REPORT_HTML}"
                    } else {
                        echo "❌ Report not found! Email will exclude it."
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                emailext(
                    subject: "Test Report - Build #${env.BUILD_NUMBER} SUCCESS",
                    body: """
                        <p>Hi Team,</p>
                        <p>The automated test execution completed successfully.</p>
                        <p>The extent report is attached along with logs and screenshots.</p>
                        <p>Regards,<br/>Automation Framework</p>
                    """,
                    mimeType: 'text/html',
                    to: "${env.EMAIL_TO}",
                    attachmentsPattern: buildAttachments()
                )
            }
        }

        failure {
            script {
                emailext(
                    subject: "Test Report - Build #${env.BUILD_NUMBER} FAILED",
                    body: """
                        <p>Hi Team,</p>
                        <p>The build failed. Please check the attached extent report, logs, and screenshots.</p>
                        <p>Regards,<br/>Automation Framework</p>
                    """,
                    mimeType: 'text/html',
                    to: "${env.EMAIL_TO}",
                    attachmentsPattern: buildAttachments()
                )
            }
        }

        always {
            echo "📦 Email and attachment processing completed."
        }
    }
}

// ✅ Helper method to include only available attachments
def buildAttachments() {
    def files = []
    if (fileExists(env.REPORT_HTML)) files << env.REPORT_HTML
    if (env.SCREENSHOT_ZIP?.trim() && fileExists(env.SCREENSHOT_ZIP)) files << env.SCREENSHOT_ZIP
    if (env.LOG_ZIP?.trim() && fileExists(env.LOG_ZIP)) files << env.LOG_ZIP
    return files.join(',')
}

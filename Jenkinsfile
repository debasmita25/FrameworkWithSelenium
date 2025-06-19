pipeline {
    agent any

    tools {
        maven 'Maven-3.9.10'
        jdk 'Java 17'
    }

    environment {
        REPORT_HTML = "target\\reports\\extent-report.html"
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

        stage('Zip Artifacts') {
            steps {
                script {
                    env.SCREENSHOT_ZIP = ''
                    env.LOG_ZIP = ''
                    def screenshotZip = "screenshots.zip"
                    def logsZip = "logs.zip"

                    // Check screenshots
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

                    // Zip logs
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
                    allowMissing: true,   // prevent failure if report is missing
                    alwaysLinkToLastBuild: true,
                    keepAll: true
                ])
            }
        }
    }

    post {
        success {
            script {
                emailext(
                    subject: "✅ Test Report - Build #${env.BUILD_NUMBER} SUCCESS",
                    body: """
                        <p>Hi Team,</p>
                        <p>The automated test execution completed <b>successfully</b>.</p>
                        <ul>
                          <li><a href="${env.BUILD_URL}HTML_20Report/">View Extent Report</a></li>
                        </ul>
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
                    subject: "❌ Test Report - Build #${env.BUILD_NUMBER} FAILED",
                    body: """
                        <p>Hi Team,</p>
                        <p>The build has <b>failed</b>. Please review the logs and reports.</p>
                        <ul>
                          <li><a href="${env.BUILD_URL}console">Console Output</a></li>
                          <li><a href="${env.BUILD_URL}HTML_20Report/">Extent Report (if available)</a></li>
                        </ul>
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

// 🧠 Helper method (only attach available files)
def buildAttachments() {
    def files = []
    if (env.REPORT_HTML?.trim()) files << env.REPORT_HTML
    if (env.SCREENSHOT_ZIP?.trim()) files << env.SCREENSHOT_ZIP
    if (env.LOG_ZIP?.trim()) files << env.LOG_ZIP
    return files.join(',')
}

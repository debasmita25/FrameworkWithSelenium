pipeline {
    agent any

    tools {
        maven 'Maven-3.9.10'
        jdk 'Java 17'
    }

    environment {
        SCREENSHOT_BASE_DIR = "target/screenshots"
        LOG_DIR = "logs"
        REPORT_NAME = "Test Execution Report"
        EMAIL_TO = "debasmita25@gmail.com"
        REPORT_HTML = ''
        SCREENSHOT_ZIP = ''
        LOG_ZIP = ''
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
    }

    post {
        always {
            script {
                // --- ZIP SCREENSHOTS ---
                def latestScreenshotFolder = powershell(returnStdout: true, script: """
                    if (Test-Path '${env.SCREENSHOT_BASE_DIR}') {
                        Get-ChildItem -Directory '${env.SCREENSHOT_BASE_DIR}' |
                        Sort-Object LastWriteTime -Descending |
                        Select-Object -First 1 |
                        Select-Object -ExpandProperty Name
                    }
                """).trim()

                if (latestScreenshotFolder) {
                    echo "📸 Found screenshot folder: ${latestScreenshotFolder}"
                    def fullPath = "${env.SCREENSHOT_BASE_DIR}/${latestScreenshotFolder}".replace('\\', '/')
                    env.SCREENSHOT_ZIP = "screenshots.zip"
                    bat """powershell -Command "Compress-Archive -Path '${fullPath}/*' -DestinationPath '${env.SCREENSHOT_ZIP}' -Force" """
                } else {
                    echo "⚠️ No screenshots found."
                }

                // --- ZIP LOGS ---
                def logExists = powershell(returnStatus: true, script: "Test-Path '${env.LOG_DIR}'")
                if (logExists == 0) {
                    env.LOG_ZIP = "logs.zip"
                    bat """powershell -Command "Compress-Archive -Path '${env.LOG_DIR}/*' -DestinationPath '${env.LOG_ZIP}' -Force" """
                } else {
                    echo "⚠️ No logs folder found."
                }

                // --- FIND HTML REPORT ---
                def reportFile = powershell(returnStdout: true, script: """
                    Get-ChildItem -Path target/reports -Filter *.html |
                    Sort-Object LastWriteTime -Descending |
                    Select-Object -First 1 |
                    Select-Object -ExpandProperty FullName
                """).trim()

                if (reportFile) {
                    reportFile = reportFile.replace('\\', '/')
                    def ws = pwd().replace('\\', '/')
                    def relativeReport = reportFile.replace(ws + '/', '')
                    env.REPORT_HTML = relativeReport
                    echo "✅ HTML report found: ${env.REPORT_HTML}"
                } else {
                    echo "⚠️ No HTML report found."
                }

                // --- ATTACHMENTS ---
                def files = []
                if (env.REPORT_HTML?.trim() && fileExists(env.REPORT_HTML)) files << env.REPORT_HTML
                if (env.SCREENSHOT_ZIP?.trim() && fileExists(env.SCREENSHOT_ZIP)) files << env.SCREENSHOT_ZIP
                if (env.LOG_ZIP?.trim() && fileExists(env.LOG_ZIP)) files << env.LOG_ZIP

                def attachPattern = files.join(',')
                echo "📎 Email attachments: ${attachPattern}"

                // --- SEND EMAIL ---
                emailext(
                    subject: "Test Report - Build #${env.BUILD_NUMBER} [${currentBuild.currentResult}]",
                    body: """
                        <p>Hi Team,</p>
                        <p>The automated test execution has completed with status: <b>${currentBuild.currentResult}</b></p>
                        <ul>
                            <li>📄 HTML Report</li>
                            <li>🖼️ Screenshots (if any)</li>
                            <li>📁 Logs (if any)</li>
                        </ul>
                        <p>📌 Download the HTML report and open in browser manually.</p>
                        <p>Regards,<br/>Automation Framework</p>
                    """,
                    mimeType: 'text/html',
                    to: "${env.EMAIL_TO}",
                    attachmentsPattern: attachPattern
                )
            }
        }
    }
}

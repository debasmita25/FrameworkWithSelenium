pipeline {
    agent any

    tools {
        maven 'Maven-3.9.10'
        jdk 'Java 17'
    }

    environment {
        SCREENSHOT_BASE_DIR = "target/screenshots"
        LOG_DIR = "logs"
        REPORT_DIR = "target/reports"
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
    }

    post {
        always {
            script {
                def attachments = []
                def workspace = pwd().replace('\\', '/')

                // ✅ Zip latest screenshots folder
                def screenshotZip = ""
                def latestScreenshotFolder = powershell(returnStdout: true, script: """
                    if (Test-Path '${env.SCREENSHOT_BASE_DIR}') {
                        Get-ChildItem -Directory '${env.SCREENSHOT_BASE_DIR}' |
                        Sort-Object LastWriteTime -Descending |
                        Select-Object -First 1 |
                        Select-Object -ExpandProperty Name
                    }
                """).trim()

                if (latestScreenshotFolder) {
                    def fullScreenshotPath = "${env.SCREENSHOT_BASE_DIR}/${latestScreenshotFolder}".replace('\\', '/')
                    screenshotZip = "screenshots.zip"
                    bat "powershell Compress-Archive -Path '${fullScreenshotPath}/*' -DestinationPath '${screenshotZip}' -Force"
                    if (fileExists(screenshotZip)) {
                        attachments << screenshotZip
                    }
                }

                // ✅ Zip logs folder
                def logsZip = ""
                def logExists = powershell(returnStatus: true, script: "Test-Path '${env.LOG_DIR}'")
                if (logExists == 0) {
                    logsZip = "logs.zip"
                    bat "powershell Compress-Archive -Path '${env.LOG_DIR}/*' -DestinationPath '${logsZip}' -Force"
                    if (fileExists(logsZip)) {
                        attachments << logsZip
                    }
                }

                // ✅ Find latest HTML report
                def reportHtml = powershell(returnStdout: true, script: """
                    if (Test-Path '${env.REPORT_DIR}') {
                        Get-ChildItem -Path '${env.REPORT_DIR}' -Filter *.html |
                        Sort-Object LastWriteTime -Descending |
                        Select-Object -First 1 |
                        Select-Object -ExpandProperty FullName
                    }
                """).trim()

                if (reportHtml) {
                    reportHtml = reportHtml.replace('\\', '/').replace("${workspace}/", "")
                    if (fileExists(reportHtml)) {
                        attachments << reportHtml
                    }
                }

                // ✅ Build attachment pattern
                def attachmentPattern = attachments.join(',')
                echo "📎 Email attachments: ${attachmentPattern}"

                // ✅ Send email with attachments
                emailext(
                    subject: "🧪 Test Report - Build #${env.BUILD_NUMBER} [${currentBuild.currentResult}]",
                    body: """
                        <p>Hi Team,</p>
                        <p>Automated test execution completed with status: <b>${currentBuild.currentResult}</b></p>
                        <p>📎 Attached: HTML report, screenshots (if any), logs (if any)</p>
                        <p>📌 Please download and open the HTML report in a browser.</p>
                        <p>Regards,<br/>Automation Framework</p>
                    """,
                    mimeType: 'text/html',
                    to: "${env.EMAIL_TO}",
                    attachmentsPattern: attachmentPattern
                )
            }
        }
    }
}

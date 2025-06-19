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

        stage('Zip Artifacts') {
            steps {
                script {
                    // Find and zip latest screenshots folder
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
                        def screenshotFullPath = "${env.SCREENSHOT_BASE_DIR}/${latestScreenshotFolder}".replace('\\', '/')
                        env.SCREENSHOT_ZIP = "screenshots.zip"
                        bat """powershell -Command "Compress-Archive -Path '${screenshotFullPath}/*' -DestinationPath '${env.SCREENSHOT_ZIP}' -Force" """
                    } else {
                        echo "⚠️ No screenshots found."
                    }

                    // Zip logs if available
                    def logCheck = powershell(returnStatus: true, script: "Test-Path '${env.LOG_DIR}'")
                    if (logCheck == 0) {
                        env.LOG_ZIP = "logs.zip"
                        bat """powershell -Command "Compress-Archive -Path '${env.LOG_DIR}/*' -DestinationPath '${env.LOG_ZIP}' -Force" """
                    } else {
                        echo "⚠️ Log folder not found. Skipping zip."
                    }
                }
            }
        }

        stage('Find HTML Report') {
            steps {
                script {
                    def reportFile = powershell(returnStdout: true, script: """
                        Get-ChildItem -Path target/reports -Filter *.html |
                        Sort-Object LastWriteTime -Descending |
                        Select-Object -First 1 |
                        Select-Object -ExpandProperty FullName
                    """).trim()

                    if (reportFile) {
                        reportFile = reportFile.replace('\\', '/')
                        def ws = pwd().replace('\\', '/')
                        def relativePath = reportFile.replace(ws + '/', '')
                        env.REPORT_HTML = relativePath
                        echo "✅ Found report: ${env.REPORT_HTML}"
                    } else {
                        echo "❌ No report found."
                    }
                }
            }
        }

        stage('Publish HTML Report') {
            steps {
                script {
                    if (env.REPORT_HTML?.trim()) {
                        publishHTML(target: [
                            reportDir: 'target/reports',
                            reportFiles: env.REPORT_HTML.tokenize('/').last(),
                            reportName: "${env.REPORT_NAME}",
                            reportTitles: 'Test Results',
                            allowMissing: true,
                            alwaysLinkToLastBuild: true,
                            keepAll: true
                        ])
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                def attachments = []

                if (env.REPORT_HTML?.trim() && fileExists(env.REPORT_HTML)) {
                    attachments << env.REPORT_HTML
                }
                if (env.SCREENSHOT_ZIP?.trim() && fileExists(env.SCREENSHOT_ZIP)) {
                    attachments << env.SCREENSHOT_ZIP
                }
                if (env.LOG_ZIP?.trim() && fileExists(env.LOG_ZIP)) {
                    attachments << env.LOG_ZIP
                }

                def attachStr = attachments.join(',')
                echo "📎 Email attachments: ${attachStr}"

                emailext(
                    subject: "🧪 Test Report - Build #${env.BUILD_NUMBER} [${currentBuild.currentResult}]",
                    body: """
                        <p>Hi Team,</p>
                        <p>The automated test execution completed with status: <b>${currentBuild.currentResult}</b>.</p>
                        <p>Attached: test report, screenshots, logs (if available).</p>
                        <p>📌 Download the HTML report before opening it in your browser.</p>
                        <p>Regards,<br/>Automation Framework</p>
                    """,
                    mimeType: 'text/html',
                    to: "${env.EMAIL_TO}",
                    attachmentsPattern: attachStr
                )
            }
        }
    }
}

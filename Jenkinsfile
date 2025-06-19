pipeline {
    agent any

    tools {
        maven 'Maven-3.9.10'
        jdk 'Java 17'
    }

    environment {
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

        stage('Find Latest HTML Report') {
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
                        env.REPORT_HTML = reportFile
                        echo "✅ Found HTML Report: ${env.REPORT_HTML}"
                    } else {
                        echo "❌ No HTML report found in target/reports"
                        env.REPORT_HTML = ''
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
                            reportFiles: env.REPORT_HTML.split('/')[-1],
                            reportName: "${env.REPORT_NAME}",
                            reportTitles: 'Test Results',
                            allowMissing: true,
                            alwaysLinkToLastBuild: true,
                            keepAll: true
                        ])
                    } else {
                        echo "⚠️ Skipping HTML Publisher because report not found."
                    }
                }
            }
        }

        stage('Confirm Report Presence') {
            steps {
                script {
                    if (env.REPORT_HTML?.trim() && fileExists(env.REPORT_HTML)) {
                        echo "✅ Report ready for email attachment."
                    } else {
                        echo "⚠️ Report not found, won't be attached."
                        env.REPORT_HTML = ''
                    }
                }
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
                        <p>The automated test execution completed successfully.</p>
                        <p>Attached: latest HTML report, screenshots, and logs (if available).</p>
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
                        <p>The build has failed. Please review the attached report, screenshots, and logs.</p>
                        <p>Regards,<br/>Automation Framework</p>
                    """,
                    mimeType: 'text/html',
                    to: "${env.EMAIL_TO}",
                    attachmentsPattern: buildAttachments()
                )
            }
        }

        always {
            echo "📬 Email step completed with available artifacts."
        }
    }
}

// ✅ Helper to attach only existing files
def buildAttachments() {
    def files = []
    def ws = pwd()

    if (env.REPORT_HTML?.trim() && fileExists(env.REPORT_HTML)) {
        def htmlRel = env.REPORT_HTML.replace(ws + "/", "")
        files << htmlRel
    }
    if (env.SCREENSHOT_ZIP?.trim() && fileExists(env.SCREENSHOT_ZIP)) {
        files << env.SCREENSHOT_ZIP
    }
    if (env.LOG_ZIP?.trim() && fileExists(env.LOG_ZIP)) {
        files << env.LOG_ZIP
    }

    echo "📎 Will attach: ${files.join(', ')}"
    return files.join(',')
}


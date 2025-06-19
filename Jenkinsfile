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
                def testSummary = ""
                def workspace = pwd().replace("\\", "/")
                def reportHtml = ""

                // ✅ Find latest extent-report.html
                reportHtml = powershell(returnStdout: true, script: """
                    Get-ChildItem -Path '${env.REPORT_DIR}' -Filter *.html |
                    Sort-Object LastWriteTime -Descending |
                    Select-Object -First 1 |
                    Select-Object -ExpandProperty FullName
                """).trim()

                if (reportHtml && fileExists(reportHtml)) {
                    reportHtml = reportHtml.replace("\\", "/").replace("${workspace}/", "")
                    attachments << reportHtml

                    // ✅ Read and parse summary from HTML
                    def rawHtml = readFile(reportHtml).replaceAll(/[\r\n]+/, " ")

                    def passedMatch = rawHtml =~ /Tests\s*Passed\s*(\d+)/
                    def failedMatch = rawHtml =~ /Tests\s*Failed\s*(\d+)/

                    def passed = passedMatch.find() ? passedMatch[0][1] as int : 0
                    def failed = failedMatch.find() ? failedMatch[0][1] as int : 0
                    def total = passed + failed
                    def skipped = 0

                    if (total > 0) {
                        testSummary = """
                            <ul>
                                <li>✅ Total: ${total}</li>
                                <li>🟢 Passed: ${passed}</li>
                                <li>🔴 Failed: ${failed}</li>
                                <li>🟡 Skipped: ${skipped}</li>
                            </ul>
                        """
                    } else {
                        testSummary = "<p>⚠️ Could not extract valid test summary from Extent Report.</p>"
                    }

                } else {
                    echo "❌ HTML report not found"
                    testSummary = "<p>❌ No HTML report found.</p>"
                }

                // ✅ Zip screenshots (latest timestamped folder)
                def latestScreenshotFolder = powershell(returnStdout: true, script: """
                    if (Test-Path '${env.SCREENSHOT_BASE_DIR}') {
                        Get-ChildItem -Directory '${env.SCREENSHOT_BASE_DIR}' |
                        Sort-Object LastWriteTime -Descending |
                        Select-Object -First 1 |
                        Select-Object -ExpandProperty Name
                    }
                """).trim()

                if (latestScreenshotFolder) {
                    def fullPath = "${env.SCREENSHOT_BASE_DIR}/${latestScreenshotFolder}".replace("\\", "/")
                    def screenshotZip = "screenshots.zip"
                    bat "powershell Compress-Archive -Path '${fullPath}/*' -DestinationPath '${screenshotZip}' -Force"
                    if (fileExists(screenshotZip)) {
                        attachments << screenshotZip
                    }
                }

                // ✅ Zip logs
                if (powershell(returnStatus: true, script: "Test-Path '${env.LOG_DIR}'") == 0) {
                    def logsZip = "logs.zip"
                    bat "powershell Compress-Archive -Path '${env.LOG_DIR}/*' -DestinationPath '${logsZip}' -Force"
                    if (fileExists(logsZip)) {
                        attachments << logsZip
                    }
                }

                // ✅ Compose and send email
                def attachmentPattern = attachments.join(',')
                echo "📎 Attachments to be sent: ${attachmentPattern}"

                emailext(
                    subject: "🧪 Test Report - Build #${env.BUILD_NUMBER} [${currentBuild.currentResult}]",
                    body: """
                        <p>Hi Team,</p>
                        <p>The automated test execution has completed with status: <strong>${currentBuild.currentResult}</strong></p>
                        <h4>📊 Test Summary:</h4>
                        ${testSummary}
                        <p>📎 Attached: HTML report, screenshots, and logs (if available).</p>
                        <p><strong>Note:</strong> Download the HTML report and open it in a browser.</p>
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
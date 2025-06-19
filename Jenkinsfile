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
        RESULT_XML = "target/surefire-reports/testng-results.xml"
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

                // ✅ Extract test summary (safe findstr)
                if (fileExists(env.RESULT_XML)) {
                    def testngLine = ""
                    try {
                        testngLine = bat(
                            script: "findstr /C:\"<testng-results\" ${env.RESULT_XML}",
                            returnStdout: true
                        ).trim()

                        def total = (testngLine =~ /total="(\d+)"/)[0][1]
                        def passed = (testngLine =~ /passed="(\d+)"/)[0][1]
                        def failed = (testngLine =~ /failed="(\d+)"/)[0][1]
                        def skipped = (testngLine =~ /skipped="(\d+)"/)[0][1]

                        testSummary = """
                            <ul>
                                <li>✅ Total: ${total}</li>
                                <li>🟢 Passed: ${passed}</li>
                                <li>🔴 Failed: ${failed}</li>
                                <li>🟡 Skipped: ${skipped}</li>
                            </ul>
                        """
                    } catch (e) {
                        echo "⚠️ Could not parse testng-results.xml: ${e}"
                        testSummary = "<p>⚠️ Error parsing test summary.</p>"
                    }
                } else {
                    echo "⚠️ testng-results.xml not found"
                    testSummary = "<p>⚠️ testng-results.xml not found. Test summary unavailable.</p>"
                }

                // ✅ Zip latest screenshots folder
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
                } else {
                    echo "⚠️ No screenshots folder found"
                }

                // ✅ Zip logs
                if (powershell(returnStatus: true, script: "Test-Path '${env.LOG_DIR}'") == 0) {
                    def logsZip = "logs.zip"
                    bat "powershell Compress-Archive -Path '${env.LOG_DIR}/*' -DestinationPath '${logsZip}' -Force"
                    if (fileExists(logsZip)) {
                        attachments << logsZip
                    }
                } else {
                    echo "⚠️ logs/ folder not found"
                }

                // ✅ Find HTML report
                def reportHtml = powershell(returnStdout: true, script: """
                    Get-ChildItem -Path '${env.REPORT_DIR}' -Filter *.html |
                    Sort-Object LastWriteTime -Descending |
                    Select-Object -First 1 |
                    Select-Object -ExpandProperty FullName
                """).trim()

                if (reportHtml) {
                    reportHtml = reportHtml.replace("\\", "/").replace("${workspace}/", "")
                    if (fileExists(reportHtml)) {
                        attachments << reportHtml
                    }
                } else {
                    echo "⚠️ HTML report not found"
                }

                // ✅ Email with attachments
                def attachmentPattern = attachments.join(',')
                echo "📎 Attachments: ${attachmentPattern}"

                emailext(
                    subject: "🧪 Test Report - Build #${env.BUILD_NUMBER} [${currentBuild.currentResult}]",
                    body: """
                        <p>Hi Team,</p>
                        <p>Test execution completed with status: <strong>${currentBuild.currentResult}</strong></p>
                        <h4>📊 Test Summary:</h4>
                        ${testSummary}
                        <p>📎 Attached: HTML report, screenshots, logs (if available)</p>
                        <p><strong>Note:</strong> Download and open the HTML report in a browser for full view.</p>
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

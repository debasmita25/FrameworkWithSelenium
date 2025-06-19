pipeline {
    agent any

    tools {
        maven 'Maven-3.9.10'
        jdk 'Java 17'
    }

    environment {
        REPORT_PATH = 'target/reports/extent-report.html'
        SCREENSHOT_DIR = 'target/screenshots'
        LOG_DIR = 'logs'
        EMAIL_TO = 'debasmita25@gmail.com'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Run Tests') {
            steps {
                bat 'mvn clean test -Dheadless=true'
            }
        }

        stage('Prepare Artifacts') {
            steps {
                script {
                    def timestamp = new Date().format("dd-MM-yyyy_HH_mm_ss")

                    // Zip latest screenshots folder
                    def screenshotFolders = new File(SCREENSHOT_DIR).listFiles().findAll { it.directory }
                    screenshotFolders.sort { -it.lastModified() }
                    def latestScreenshot = screenshotFolders ? screenshotFolders[0] : null
                    if (latestScreenshot) {
                        def screenshotZip = "screenshots_${timestamp}.zip"
                        bat "powershell -Command \"Compress-Archive -Path '${latestScreenshot}/' -DestinationPath '${screenshotZip}' -Force\""
                        env.SCREENSHOT_ZIP = screenshotZip
                    } else {
                        echo 'No screenshots found.'
                        env.SCREENSHOT_ZIP = ''
                    }

                    // Zip logs
                    def logZip = "logs_${timestamp}.zip"
                    if (fileExists(LOG_DIR)) {
                        bat "powershell -Command \"Compress-Archive -Path '${LOG_DIR}/*' -DestinationPath '${logZip}' -Force\""
                        env.LOG_ZIP = logZip
                    } else {
                        echo 'No logs found.'
                        env.LOG_ZIP = ''
                    }

                    // Confirm report
                    if (!fileExists(REPORT_PATH)) {
                        echo '❌ HTML report not found.'
                        env.REPORT_PATH = ''
                    }
                }
            }
        }

        stage('Publish Report') {
            steps {
                script {
                    if (fileExists(REPORT_PATH)) {
                        publishHTML(target: [
                            reportDir: 'target/reports',
                            reportFiles: 'extent-report.html',
                            reportName: 'Test Report',
                            alwaysLinkToLastBuild: true,
                            keepAll: true
                        ])
                    } else {
                        echo '⚠️ No report to publish.'
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                // Build test summary from report HTML
                def summary = '⚠️ Could not extract valid test summary from Extent Report.'
                if (fileExists(REPORT_PATH)) {
                    def content = readFile(REPORT_PATH)
                    def passed = content =~ /<p class=\"m-b-0 text-pass\">Tests Passed<\/p>\s*<h3>(\\d+)<\/h3>/
                    def failed = content =~ /<p class=\"m-b-0 text-fail\">Tests Failed<\/p>\s*<h3>(\\d+)<\/h3>/
                    if (passed.find() && failed.find()) {
                        summary = """
✅ Test Summary:<br/>
Passed: ${passed[0][1]}<br/>
Failed: ${failed[0][1]}<br/>
                        """
                    }
                }

                // Attachments
                def attachments = []
                if (fileExists(REPORT_PATH)) attachments << REPORT_PATH
                if (env.SCREENSHOT_ZIP?.trim() && fileExists(env.SCREENSHOT_ZIP)) attachments << env.SCREENSHOT_ZIP
                if (env.LOG_ZIP?.trim() && fileExists(env.LOG_ZIP)) attachments << env.LOG_ZIP

                def attachmentPattern = attachments.join(',')
                echo "📎 Email attachments: ${attachmentPattern}"

                // Email notification
                def status = currentBuild.currentResult
                emailext(
                    subject: "🧪 Test Report - Build #${env.BUILD_NUMBER} [${status}]",
                    body: """
<p>Hi Team,</p>
<p>The automated test execution has completed with status: <b>${status}</b></p>
<p>${summary}</p>
<p>📎 Attached: HTML report, screenshots, and logs (if available).</p>
<p>Note: Download the HTML report and open it in a browser.</p>
<p>Regards,<br/>Automation Framework</p>
                    """,
                    mimeType: 'text/html',
                    to: EMAIL_TO,
                    attachmentsPattern: attachmentPattern
                )
            }
        }
    }
}

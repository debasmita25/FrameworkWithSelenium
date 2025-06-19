pipeline {
    agent any

    tools {
        maven 'Maven-3.9.10'
        jdk 'Java 17'
    }

    environment {
        SCREENSHOT_BASE_DIR = "target/screenshots"
        REPORT_PATH = "target/reports/extent-report.html"
        LOG_DIR = "logs"
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

        stage('Prepare Artifacts') {
            steps {
                script {
                    // Screenshot zip
                    def latestFolder = powershell(returnStdout: true, script: """
                        if (Test-Path '${env.SCREENSHOT_BASE_DIR}') {
                            Get-ChildItem '${env.SCREENSHOT_BASE_DIR}' -Directory |
                            Sort-Object LastWriteTime -Descending |
                            Select-Object -First 1 |
                            Select-Object -ExpandProperty Name
                        }
                    """).trim()
                    if (latestFolder) {
                        def src = "${env.SCREENSHOT_BASE_DIR}/${latestFolder}"
                        def zip = "screenshots.zip"
                        bat "powershell Compress-Archive -Path '${src}/*' -DestinationPath '${zip}' -Force"
                        env.SCREENSHOT_ZIP = zip
                        echo "📸 Zipped screenshots from: ${src}"
                    } else {
                        echo "⚠️ No screenshot folders found."
                    }

                    // Logs zip
                    if (fileExists(env.LOG_DIR)) {
                        def zip = "logs.zip"
                        bat "powershell Compress-Archive -Path '${env.LOG_DIR}/*' -DestinationPath '${zip}' -Force"
                        env.LOG_ZIP = zip
                        echo "📝 Zipped logs from: ${env.LOG_DIR}"
                    } else {
                        echo "⚠️ No logs found to zip."
                    }
                }
            }
        }

        stage('Summarize Test Results') {
            steps {
                script {
                    env.TEST_SUMMARY = extractSummary(env.REPORT_PATH)
                    echo "📊 Test Summary: ${env.TEST_SUMMARY}"
                }
            }
        }
    }

    post {
        always {
            script {
                def attachments = []

                def reportExists = fileExists(REPORT_PATH)
                if (reportExists) attachments << REPORT_PATH
                if (env.SCREENSHOT_ZIP && fileExists(env.SCREENSHOT_ZIP)) attachments << env.SCREENSHOT_ZIP
                if (env.LOG_ZIP && fileExists(env.LOG_ZIP)) attachments << env.LOG_ZIP

                def status = currentBuild.currentResult
                def color = status == 'SUCCESS' ? '#28a745' : '#dc3545'
                def subject = "🧪 Test Report - Build #${env.BUILD_NUMBER} [${status}]"

                emailext(
                    subject: subject,
                    body: """
                        <p>Hi Team,</p>
                        <p>The automated test execution has completed with status: <b style='color:${color}'>${status}</b></p>

                        <p>📊 <b>Test Summary:</b><br/>
                        ${env.TEST_SUMMARY.replaceAll('\n', '<br/>')}
                        </p>

                        <p>📎 Attached: HTML report, screenshots, and logs (if available).</p>
                        <p><i>Note: Download the HTML report and open it in a browser.</i></p>

                        <p>Regards,<br/>Automation Framework</p>
                    """,
                    mimeType: 'text/html',
                    to: "${env.EMAIL_TO}",
                    attachmentsPattern: attachments.join(',')
                )
            }
        }
    }
}

// ✅ Helper function to extract summary from extent-report.html
def extractSummary(reportPath) {
    if (!fileExists(reportPath)) return "⚠️ HTML report not found."

    try {
        def content = readFile(reportPath)

        def passMatch = content =~ /<p class="m-b-0 text-pass">.*?<\/p>\s*<h3>(\d+)<\/h3>/
        def failMatch = content =~ /<p class="m-b-0 text-fail">.*?<\/p>\s*<h3>(\d+)<\/h3>/

        def passed = passMatch ? passMatch[0][1] : "0"
        def failed = failMatch ? failMatch[0][1] : "0"

        return """✅ Tests Passed: ${passed}
❌ Tests Failed: ${failed}"""

    } catch (e) {
        echo "❌ Summary extract failed: ${e.message}"
        return "⚠️ Could not extract valid test summary from Extent Report."
    }
}

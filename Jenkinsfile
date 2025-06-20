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
        stage('Show Input') {
            steps {
                echo "User selected ENVIRONMENT = ${params.TEST_ENV}"
                echo "User selected SUITE = ${params.TEST_SUITE}" 
            }
        }

        stage('Checkout') {
            steps {   
                checkout scm
            }
        }

        stage('Run Tests') {
            steps {
                bat "powershell -Command \"& { mvn clean test -P${params.TEST_ENV} -DsuiteXmlFile='${params.TEST_SUITE}' | Tee-Object -FilePath test-output.log }\""
            }
        }
    }

    post {
        always {
            script {
                def attachments = []
                def workspace = pwd().replace('\\', '/')

                // ✅ Latest HTML report
                def reportHtml = powershell(returnStdout: true, script: """
                    if (Test-Path '${env.REPORT_DIR}') {
                        Get-ChildItem -Path '${env.REPORT_DIR}' -Filter *.html |
                        Sort-Object LastWriteTime -Descending |
                        Select-Object -First 1 |
                        Select-Object -ExpandProperty FullName
                    }
                """).trim()

                def testSummary = ""
                if (reportHtml && fileExists(reportHtml)) {
                    reportHtml = reportHtml.replace('\\', '/').replace("${workspace}/", "")
                    def htmlContent = readFile(reportHtml)

                    def passMatch = htmlContent =~ /<p class="m-b-0 text-pass">Tests Passed<\/p>\s*<h3>(\d+)<\/h3>/
                    def failMatch = htmlContent =~ /<p class="m-b-0 text-fail">Tests Failed<\/p>\s*<h3>(\d+)<\/h3>/

                    def passed = passMatch.find() ? passMatch[0][1] : "0"
                    def failed = failMatch.find() ? failMatch[0][1] : "0"

                    testSummary = "Tests Passed: ${passed}, Tests Failed: ${failed}"
                    attachments << reportHtml
                    echo "✅ Extracted test summary from HTML: ${testSummary}"
                } else {
                    echo "❌ HTML report not found at: ${reportHtml}"
                    testSummary = "Tests run: N/A"
                }

                // ✅ Calculate build info
                def buildStart = new Date(currentBuild.getStartTimeInMillis())
                def formattedDate = buildStart.format("EEE, dd MMM yyyy HH:mm:ss Z", TimeZone.getTimeZone('IST'))
                def durationMillis = System.currentTimeMillis() - currentBuild.getStartTimeInMillis()
                def minutes = (int)(durationMillis / 60000)
                def seconds = (int)((durationMillis % 60000) / 1000)
                def durationStr = "${minutes} min ${seconds} sec"
                def cause = currentBuild.getBuildCauses()[0]?.shortDescription ?: 'Manual trigger'

                // ✅ Build Summary Note for Email
                def buildNote = """
                    <pre>
Build Summary:
${testSummary}
Project: ${env.JOB_NAME}
Date: ${formattedDate}
Duration: ${durationStr}
Cause: ${cause}
                    </pre>
                """

                // ✅ Zip latest screenshots
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
                    def screenshotZip = "screenshots.zip"
                    bat "if exist ${screenshotZip} del ${screenshotZip}"
                    bat "powershell Compress-Archive -Path '${fullScreenshotPath}/*' -DestinationPath '${screenshotZip}' -Force"
                    if (fileExists(screenshotZip)) {
                        attachments << screenshotZip
                    }
                }

                // ✅ Zip only logs/app.log
def logsZip = "app-log.zip"
def appLogPath = "${env.LOG_DIR}/app.log".replace('\\', '/')

if (fileExists(appLogPath)) {
    bat "if exist ${logsZip} del ${logsZip}"
    bat "powershell Compress-Archive -Path '${appLogPath}' -DestinationPath '${logsZip}' -Force"
    if (fileExists(logsZip)) {
        attachments << logsZip
    }
} else {
    echo "❌ app.log not found at: ${appLogPath}"
}
                
				  
                def attachmentPattern = attachments.join(',')
                echo "📎 Email attachments: ${attachmentPattern ?: 'None'}"

                // ✅ Send Email
                emailext(
                    subject: "Test Report - Build #${env.BUILD_NUMBER} [${currentBuild.currentResult}]",
                    body: """
                        <p>Hi Team,</p>
                        <p>Automated test execution completed with status: <b>${currentBuild.currentResult}</b></p>
                        ${buildNote}
                        <p>Attached: HTML report, screenshots (if any), logs (if any)</p>
                        <p>Please download and open the HTML report in a browser.</p>
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

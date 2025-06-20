pipeline {
    agent any

    environment {
        SCREENSHOT_BASE_DIR = "target\\screenshots"
        LOG_DIR = "logs"
        REPORT_DIR = "target\\reports"
        EMAIL_TO = "debasmita25@gmail.com"
    }

    stages {
        stage('Run Tests') {
            steps {
                bat "mvn clean test -Dheadless=true | tee test-output.log"
            }
        }

        stage('Print Test Summary') {
            steps {
                bat '''
                echo ====== Test Summary ======
                echo NOTE: Please download the TestReport on your machine first and open it to view it.
                findstr /R /C:"Tests run:.*Failures:.*Errors:.*Skipped:" test-output.log
                '''
            }
        }

        stage('Build Summary') {
            steps {
                script {
                    def buildTime = new Date(currentBuild.getStartTimeInMillis())
                        .format("EEE, dd MMM yyyy HH:mm:ss Z", TimeZone.getTimeZone('IST'))
                    def durationMillis = System.currentTimeMillis() - currentBuild.getStartTimeInMillis()
                    def minutes = (int)(durationMillis / 60000)
                    def seconds = (int)((durationMillis % 60000) / 1000)
                    def durationStr = "${minutes} min ${seconds} sec"

                    echo """
                    ====== BUILD COMPLETED ======
                    URL:        ${env.BUILD_URL}
                    Project:    ${env.JOB_NAME}
                    Date:       ${buildTime}
                    Duration:   ${durationStr}
                    Cause:      ${currentBuild.getBuildCauses()[0]?.shortDescription ?: 'Manual trigger'}
                    """
                }
            }
        }

        stage('Prepare Attachments') {
            steps {
                script {
                    // ===== Find latest TestReport_<timestamp>.html =====
                    def reportFile = bat(
                        script: """
                        @echo off
                        for /f %%i in ('dir "${REPORT_DIR}\\TestReport_*.html" /b /o-d') do (
                            echo ${REPORT_DIR}\\%%i
                            goto :done
                        )
                        :done
                        """,
                        returnStdout: true
                    ).trim()

                    echo "Latest HTML Report: ${reportFile}"
                    env.REPORT_PATH = reportFile.replace("\\", "/") // normalize slashes

                    // ===== Find latest screenshot folder =====
                    def latestScreenshotFolder = bat(
                        script: """
                        @echo off
                        setlocal EnableDelayedExpansion
                        set "newest="
                        for /f %%F in ('dir "${SCREENSHOT_BASE_DIR}" /b /ad /o-d') do (
                            set "newest=%%F"
                            goto found
                        )
                        :found
                        echo !newest!
                        """,
                        returnStdout: true
                    ).trim()

                    echo "Latest screenshot folder: ${latestScreenshotFolder}"

                    // ===== Zip latest screenshots =====
                    bat """
                    if exist zipped_screenshots.zip del zipped_screenshots.zip
                    powershell Compress-Archive -Path "${SCREENSHOT_BASE_DIR}\\${latestScreenshotFolder}\\*" -DestinationPath zipped_screenshots.zip
                    """

                    // ===== Zip logs =====
                    bat """
                    if exist zipped_logs.zip del zipped_logs.zip
                    powershell Compress-Archive -Path "${LOG_DIR}\\*" -DestinationPath zipped_logs.zip
                    """
                }
            }
        }
    }

    post {
        always {
            emailext(
                subject: "Test Report: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                body: """
                    <p>Hi Team,</p>
                    <p>Test execution completed with status: <b>${currentBuild.currentResult}</b></p>
                    <p><b>Build Info:</b><br/>
                    Project: ${env.JOB_NAME}<br/>
                    Build URL: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a><br/>
                    Build #: ${env.BUILD_NUMBER}</p>
                    <p><b>Note:</b> Please download the HTML report and open it in a browser.</p>
                    <p>Regards,<br/>Automation Framework</p>
                """,
                to: "${env.EMAIL_TO}",
                attachmentsPattern: "${env.REPORT_PATH}, zipped_logs.zip, zipped_screenshots.zip"
            )
        }
    }
}

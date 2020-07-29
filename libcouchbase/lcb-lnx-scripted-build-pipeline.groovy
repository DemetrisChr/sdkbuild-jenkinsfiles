
// DO NOT EDIT: this file was generated from Jenkinsfile.erb
class Version {
    String gitVersion;

    int major;
    int minor;
    int patch;
    int commitCount;
    String prerelease;
    String commitSha1;

    Version(String gitVersion) {
        this.gitVersion = gitVersion.trim();
        parse()
    }

    @NonCPS
    void parse() {
        def res = (gitVersion =~ /^(\d+)\.(\d+)\.(\d+)(-(beta.\d+))?(-(\d+)-g([0-9a-f]+))?$/)
        res.find()
        this.major = res.group(1) as Integer
        this.minor = res.group(2) as Integer
        this.patch = res.group(3) as Integer
        if (res.group(5)) {
            this.prerelease = res.group(5)
        }
        if (res.group(7)) {
            this.commitCount = res.group(7) as Integer
        }
        if (res.group(8)) {
            this.commitSha1 = res.group(8)
        }
    }

    String version() {
        return "${major}.${minor}.${patch}"
    }

    String tar() {
        if (commitCount == null || commitCount == 0) {
            if (prerelease != null && prerelease != "") {
                return "${version()}_${prerelease}"
            } else {
                return version()
            }
        }
        return gitVersion.replace("-", "_")
    }

    String tarName() {
        return "libcouchbase-${tar()}"
    }

    String rpmVer() {
        return version()
    }

    String rpmRel() {
        def rel = "1"
        if (prerelease) {
            rel = "0.${prerelease}"
        } else if (commitCount) {
            rel = "${commitCount + 1}.git${commitSha1}"
        }
        return rel
    }

    String srpmGlob() {
        return "libcouchbase-${version()}-${rpmRel()}*.src.rpm"
    }

    String[] rpm() {
        return [version(), rpmRel()]
    }

    String deb() {
        def ver = version()
        if (prerelease) {
            ver += "~${prerelease}"
        } else if (commitCount) {
            ver += "~r${commitCount}git${commitSha1}"
        }
        return ver
    }
}

def VERSION = new Version('0.0.0')

class DynamicCluster {
    String id = null;
    String connstr = null;
    String server_version = null;

    boolean isAllocated() {
        return !(id == null || id == "")
    }

    String inspect() {
        return "Cluster(id: ${id}, connstr: ${connstr})"
    }
}

def CLUSTER = [:]

def doIntegrationStages(CLUSTER) {
    def returned_stages = [:]
    returned_stages['5.5.6'] = {
        node('sdkqe-centos7') {
            try {
                stage('start') {
                    sh("cbdyncluster ps -a")
                    script {
                       def cluster = new DynamicCluster()
                        CLUSTER['5.5.6'] = cluster
                        def ver = '5.5.6'.tokenize("_")[0]
                        cluster.id = sh(script: "cbdyncluster allocate --num-nodes=3 --server-version=${ver}", returnStdout: true).trim()
                        cluster.connstr = sh(script: "cbdyncluster ips ${cluster.id}", returnStdout: true).trim()
                    }
                    echo "Allocated ${CLUSTER['5.5.6'].inspect()}"
                    sh("cbdyncluster setup ${CLUSTER['5.5.6'].id} --node=kv,index,n1ql,fts --node=kv --node=kv --bucket=default")
                    script {
                        def isDp = '5.5.6'.tokenize("_").size() > 1
                        def ip = "${CLUSTER['5.5.6'].connstr}".tokenize(",")[0]
                        echo "isDp=${isDp}"
                        sh("""curl -vv -X POST -u Administrator:password http://${ip}:8091/sampleBuckets/install -d '["beer-sample"]'""")
                        sleep(30)
                        if (isDp) {
                           sh("curl -vv -X POST -u Administrator:password http://${ip}:8091/settings/developerPreview -d 'enabled=true'")
                        }
                        stage('test') {
                            try {
                                environment {
                                    LCB_TEST_CLUSTER_CONF="${CLUSTER['5.5.6'].connstr.replaceAll(',', ';')},default,Administrator,password"
                                }
                                unstash('centos7_build')
                                dir('ws_centos7_x64/build') {
                                    sh("pwd")
                                    sh("sed -i s:/home/couchbase/jenkins/workspace/lcb/lcb-scripted-build-pipeline/ws_centos7_x64/build:\$(realpath .):g tests/CTestTestfile.cmake")
                                    sleep(20)
                                    sh("ctest -E BUILD ${VERBOSE.toBoolean() ? '-VV' : ''}")
                                }
                            } catch(all) {
                                sh('tar cf integration_failure-ws_centos7_5.5.6_x64.tar ws_centos7_x64')
                                archiveArtifacts(artifacts: "integration_failure-ws_centos7_5.5.6_x64.tar")
                            }
                        }
                    }
                }
            } finally {
                script {
                    if (CLUSTER['5.5.6'] && CLUSTER['5.5.6'].isAllocated()) {
                        sh("cbdyncluster rm ${CLUSTER['5.5.6'].id}")
                    }
                }
            }
        }
    }
    returned_stages['6.0.4'] = {
        node('sdkqe-centos7') {
            try {
                stage('start') {
                    sh("cbdyncluster ps -a")
                    script {
                       def cluster = new DynamicCluster()
                        CLUSTER['6.0.4'] = cluster
                        def ver = '6.0.4'.tokenize("_")[0]
                        cluster.id = sh(script: "cbdyncluster allocate --num-nodes=3 --server-version=${ver}", returnStdout: true).trim()
                        cluster.connstr = sh(script: "cbdyncluster ips ${cluster.id}", returnStdout: true).trim()
                    }
                    echo "Allocated ${CLUSTER['6.0.4'].inspect()}"
                    sh("cbdyncluster setup ${CLUSTER['6.0.4'].id} --node=kv,index,n1ql,fts --node=kv --node=kv --bucket=default")
                    script {
                        def isDp = '6.0.4'.tokenize("_").size() > 1
                        def ip = "${CLUSTER['6.0.4'].connstr}".tokenize(",")[0]
                        echo "isDp=${isDp}"
                        sh("""curl -vv -X POST -u Administrator:password http://${ip}:8091/sampleBuckets/install -d '["beer-sample"]'""")
                        sleep(30)
                        if (isDp) {
                           sh("curl -vv -X POST -u Administrator:password http://${ip}:8091/settings/developerPreview -d 'enabled=true'")
                        }
                        stage('test') {
                            try {
                                environment {
                                    LCB_TEST_CLUSTER_CONF="${CLUSTER['6.0.4'].connstr.replaceAll(',', ';')},default,Administrator,password"
                                }
                                unstash('centos7_build')
                                dir('ws_centos7_x64/build') {
                                    sh("pwd")
                                    sh("sed -i s:/home/couchbase/jenkins/workspace/lcb/lcb-scripted-build-pipeline/ws_centos7_x64/build:\$(realpath .):g tests/CTestTestfile.cmake")
                                    sleep(20)
                                    sh("ctest -E BUILD ${VERBOSE.toBoolean() ? '-VV' : ''}")
                                }
                            } catch(all) {
                                sh('tar cf integration_failure-ws_centos7_6.0.4_x64.tar ws_centos7_x64')
                                archiveArtifacts(artifacts: "integration_failure-ws_centos7_6.0.4_x64.tar")
                            }
                        }
                    }
                }
            } finally {
                script {
                    if (CLUSTER['6.0.4'] && CLUSTER['6.0.4'].isAllocated()) {
                        sh("cbdyncluster rm ${CLUSTER['6.0.4'].id}")
                    }
                }
            }
        }
    }
    returned_stages['6.5.1'] = {
        node('sdkqe-centos7') {
            try {
                stage('start') {
                    sh("cbdyncluster ps -a")
                    script {
                       def cluster = new DynamicCluster()
                        CLUSTER['6.5.1'] = cluster
                        def ver = '6.5.1'.tokenize("_")[0]
                        cluster.id = sh(script: "cbdyncluster allocate --num-nodes=3 --server-version=${ver}", returnStdout: true).trim()
                        cluster.connstr = sh(script: "cbdyncluster ips ${cluster.id}", returnStdout: true).trim()
                    }
                    echo "Allocated ${CLUSTER['6.5.1'].inspect()}"
                    sh("cbdyncluster setup ${CLUSTER['6.5.1'].id} --node=kv,index,n1ql,fts --node=kv --node=kv --bucket=default")
                    script {
                        def isDp = '6.5.1'.tokenize("_").size() > 1
                        def ip = "${CLUSTER['6.5.1'].connstr}".tokenize(",")[0]
                        echo "isDp=${isDp}"
                        sh("""curl -vv -X POST -u Administrator:password http://${ip}:8091/sampleBuckets/install -d '["beer-sample"]'""")
                        sleep(30)
                        if (isDp) {
                           sh("curl -vv -X POST -u Administrator:password http://${ip}:8091/settings/developerPreview -d 'enabled=true'")
                        }
                        stage('test') {
                            try {
                                environment {
                                    LCB_TEST_CLUSTER_CONF="${CLUSTER['6.5.1'].connstr.replaceAll(',', ';')},default,Administrator,password"
                                }
                                unstash('centos7_build')
                                dir('ws_centos7_x64/build') {
                                    sh("pwd")
                                    sh("sed -i s:/home/couchbase/jenkins/workspace/lcb/lcb-scripted-build-pipeline/ws_centos7_x64/build:\$(realpath .):g tests/CTestTestfile.cmake")
                                    sleep(20)
                                    sh("ctest -E BUILD ${VERBOSE.toBoolean() ? '-VV' : ''}")
                                }
                            } catch(all) {
                                sh('tar cf integration_failure-ws_centos7_6.5.1_x64.tar ws_centos7_x64')
                                archiveArtifacts(artifacts: "integration_failure-ws_centos7_6.5.1_x64.tar")
                            }
                        }
                    }
                }
            } finally {
                script {
                    if (CLUSTER['6.5.1'] && CLUSTER['6.5.1'].isAllocated()) {
                        sh("cbdyncluster rm ${CLUSTER['6.5.1'].id}")
                    }
                }
            }
        }
    }
    returned_stages['6.5.1_DP'] = {
        node('sdkqe-centos7') {
            try {
                stage('start') {
                    sh("cbdyncluster ps -a")
                    script {
                       def cluster = new DynamicCluster()
                        CLUSTER['6.5.1_DP'] = cluster
                        def ver = '6.5.1_DP'.tokenize("_")[0]
                        cluster.id = sh(script: "cbdyncluster allocate --num-nodes=3 --server-version=${ver}", returnStdout: true).trim()
                        cluster.connstr = sh(script: "cbdyncluster ips ${cluster.id}", returnStdout: true).trim()
                    }
                    echo "Allocated ${CLUSTER['6.5.1_DP'].inspect()}"
                    sh("cbdyncluster setup ${CLUSTER['6.5.1_DP'].id} --node=kv,index,n1ql,fts --node=kv --node=kv --bucket=default")
                    script {
                        def isDp = '6.5.1_DP'.tokenize("_").size() > 1
                        def ip = "${CLUSTER['6.5.1_DP'].connstr}".tokenize(",")[0]
                        echo "isDp=${isDp}"
                        sh("""curl -vv -X POST -u Administrator:password http://${ip}:8091/sampleBuckets/install -d '["beer-sample"]'""")
                        sleep(30)
                        if (isDp) {
                           sh("curl -vv -X POST -u Administrator:password http://${ip}:8091/settings/developerPreview -d 'enabled=true'")
                        }
                        stage('test') {
                            try {
                                environment {
                                    LCB_TEST_CLUSTER_CONF="${CLUSTER['6.5.1_DP'].connstr.replaceAll(',', ';')},default,Administrator,password"
                                }
                                unstash('centos7_build')
                                dir('ws_centos7_x64/build') {
                                    sh("pwd")
                                    sh("sed -i s:/home/couchbase/jenkins/workspace/lcb/lcb-scripted-build-pipeline/ws_centos7_x64/build:\$(realpath .):g tests/CTestTestfile.cmake")
                                    sleep(20)
                                    sh("ctest -E BUILD ${VERBOSE.toBoolean() ? '-VV' : ''}")
                                }
                            } catch(all) {
                                sh('tar cf integration_failure-ws_centos7_6.5.1_DP_x64.tar ws_centos7_x64')
                                archiveArtifacts(artifacts: "integration_failure-ws_centos7_6.5.1_DP_x64.tar")
                            }
                        }
                    }
                }
            } finally {
                script {
                    if (CLUSTER['6.5.1_DP'] && CLUSTER['6.5.1_DP'].isAllocated()) {
                        sh("cbdyncluster rm ${CLUSTER['6.5.1_DP'].id}")
                    }
                }
            }
        }
    }
    returned_stages['6.6-stable'] = {
        node('sdkqe-centos7') {
            try {
                stage('start') {
                    sh("cbdyncluster ps -a")
                    script {
                       def cluster = new DynamicCluster()
                        CLUSTER['6.6-stable'] = cluster
                        def ver = '6.6-stable'.tokenize("_")[0]
                        cluster.id = sh(script: "cbdyncluster allocate --num-nodes=3 --server-version=${ver}", returnStdout: true).trim()
                        cluster.connstr = sh(script: "cbdyncluster ips ${cluster.id}", returnStdout: true).trim()
                    }
                    echo "Allocated ${CLUSTER['6.6-stable'].inspect()}"
                    sh("cbdyncluster setup ${CLUSTER['6.6-stable'].id} --node=kv,index,n1ql,fts --node=kv --node=kv --bucket=default")
                    script {
                        def isDp = '6.6-stable'.tokenize("_").size() > 1
                        def ip = "${CLUSTER['6.6-stable'].connstr}".tokenize(",")[0]
                        echo "isDp=${isDp}"
                        sh("""curl -vv -X POST -u Administrator:password http://${ip}:8091/sampleBuckets/install -d '["beer-sample"]'""")
                        sleep(30)
                        if (isDp) {
                           sh("curl -vv -X POST -u Administrator:password http://${ip}:8091/settings/developerPreview -d 'enabled=true'")
                        }
                        stage('test') {
                            try {
                                environment {
                                    LCB_TEST_CLUSTER_CONF="${CLUSTER['6.6-stable'].connstr.replaceAll(',', ';')},default,Administrator,password"
                                }
                                unstash('centos7_build')
                                dir('ws_centos7_x64/build') {
                                    sh("pwd")
                                    sh("sed -i s:/home/couchbase/jenkins/workspace/lcb/lcb-scripted-build-pipeline/ws_centos7_x64/build:\$(realpath .):g tests/CTestTestfile.cmake")
                                    sleep(20)
                                    sh("ctest -E BUILD ${VERBOSE.toBoolean() ? '-VV' : ''}")
                                }
                            } catch(all) {
                                sh('tar cf integration_failure-ws_centos7_6.6-stable_x64.tar ws_centos7_x64')
                                archiveArtifacts(artifacts: "integration_failure-ws_centos7_6.6-stable_x64.tar")
                            }
                        }
                    }
                }
            } finally {
                script {
                    if (CLUSTER['6.6-stable'] && CLUSTER['6.6-stable'].isAllocated()) {
                        sh("cbdyncluster rm ${CLUSTER['6.6-stable'].id}")
                    }
                }
            }
        }
    }
    returned_stages['7.0-stable'] = {
        node('sdkqe-centos7') {
            try {
                stage('start') {
                    sh("cbdyncluster ps -a")
                    script {
                       def cluster = new DynamicCluster()
                        CLUSTER['7.0-stable'] = cluster
                        def ver = '7.0-stable'.tokenize("_")[0]
                        cluster.id = sh(script: "cbdyncluster allocate --num-nodes=3 --server-version=${ver}", returnStdout: true).trim()
                        cluster.connstr = sh(script: "cbdyncluster ips ${cluster.id}", returnStdout: true).trim()
                    }
                    echo "Allocated ${CLUSTER['7.0-stable'].inspect()}"
                    sh("cbdyncluster setup ${CLUSTER['7.0-stable'].id} --node=kv,index,n1ql,fts --node=kv --node=kv --bucket=default")
                    script {
                        def isDp = '7.0-stable'.tokenize("_").size() > 1
                        def ip = "${CLUSTER['7.0-stable'].connstr}".tokenize(",")[0]
                        echo "isDp=${isDp}"
                        sh("""curl -vv -X POST -u Administrator:password http://${ip}:8091/sampleBuckets/install -d '["beer-sample"]'""")
                        sleep(30)
                        if (isDp) {
                           sh("curl -vv -X POST -u Administrator:password http://${ip}:8091/settings/developerPreview -d 'enabled=true'")
                        }
                        stage('test') {
                            try {
                                environment {
                                    LCB_TEST_CLUSTER_CONF="${CLUSTER['7.0-stable'].connstr.replaceAll(',', ';')},default,Administrator,password"
                                }
                                unstash('centos7_build')
                                dir('ws_centos7_x64/build') {
                                    sh("pwd")
                                    sh("sed -i s:/home/couchbase/jenkins/workspace/lcb/lcb-scripted-build-pipeline/ws_centos7_x64/build:\$(realpath .):g tests/CTestTestfile.cmake")
                                    sleep(20)
                                    sh("ctest -E BUILD ${VERBOSE.toBoolean() ? '-VV' : ''}")
                                }
                            } catch(all) {
                                sh('tar cf integration_failure-ws_centos7_7.0-stable_x64.tar ws_centos7_x64')
                                archiveArtifacts(artifacts: "integration_failure-ws_centos7_7.0-stable_x64.tar")
                            }
                        }
                    }
                }
            } finally {
                script {
                    if (CLUSTER['7.0-stable'] && CLUSTER['7.0-stable'].isAllocated()) {
                        sh("cbdyncluster rm ${CLUSTER['7.0-stable'].id}")
                    }
                }
            }
        }
    }
    return returned_stages
}

pipeline {
    agent none
    stages {
        stage('prepare and validate') {
            agent { label 'centos8 || centos7 || centos6' }
            steps {
                cleanWs()
                script {
                    if (IS_GERRIT_TRIGGER.toBoolean()) {
                        currentBuild.displayName = "cv-${BUILD_NUMBER}"
                    } else {
                        currentBuild.displayName = "full-${BUILD_NUMBER}"
                    }
                }

                dir('libcouchbase') {
                    checkout([$class: 'GitSCM', branches: [[name: '$SHA']], userRemoteConfigs: [[refspec: "$GERRIT_REFSPEC", url: '$REPO', poll: false]]])
                    script {
                        VERSION = new Version(sh(script: 'git describe --long --abbrev=10', returnStdout: true))
                        echo "Building ${VERSION.gitVersion}, gerrit: ${IS_GERRIT_TRIGGER}, release: ${IS_RELEASE}"
                    }
                }

                stash includes: 'libcouchbase/', name: 'libcouchbase', useDefaultExcludes: false

                dir('libcouchbase') {
                    dir('build') {
                        sh('cmake -DCMAKE_BUILD_TYPE=RelWithDebInfo -DLCB_NO_PLUGINS=1 -DLCB_NO_TESTS=1 -DLCB_NO_MOCK=1 ..')
                        sh('make dist')
                        archiveArtifacts(artifacts: "${VERSION.tarName()}.tar.gz", fingerprint: true)
                        stash includes: "${VERSION.tarName()}.tar.gz", name: 'tarball', useDefaultExcludes: false
                    }
                }
            }
        }

        stage('build and test') {
            parallel {

                stage('debian9 mock') {
                    agent { label 'debian9' }
                    stages {
                        stage('deb9') {
                            steps {
                                dir('ws_debian9_x64') {
                                    deleteDir()
                                    unstash 'libcouchbase'
                                }
                            }
                        }
                        stage('build') {
                            post {
                                failure {
                                    sh('tar cf failure-ws_debian9_x64.tar ws_debian9_x64')
                                    archiveArtifacts(artifacts: "failure-ws_debian9_x64.tar", fingerprint: false)
                                }
                            }
                            steps {
                                dir('ws_debian9_x64') {
                                    dir('build') {
                                        sh('cmake -DCMAKE_BUILD_TYPE=RelWithDebInfo ../libcouchbase')
                                        sh("make -j8 ${VERBOSE.toBoolean() ? 'VERBOSE=1' : ''}")
                                        sh("make -j8 ${VERBOSE.toBoolean() ? 'VERBOSE=1' : ''} alltests")
                                    }
                                }
                                stash includes: 'ws_debian9_x64/', name: 'debian9_build'
                            }
                        }
                        stage('test') {
                            post {
                                failure {
                                    sh('tar cf failure-ws_debian9_x64.tar ws_debian9_x64')
                                    archiveArtifacts(artifacts: "failure-ws_debian9_x64.tar", fingerprint: false)
                                }
                                always {
                                    junit("ws_debian9_x64/build/*.xml")
                                }
                            }
                            steps {
                                dir('ws_debian9_x64/build') {
                                    sh("ctest ${VERBOSE.toBoolean() ? '-VV' : ''}")
                                }
                            }
                        }
                    }
                }
                stage('centos7 mock') {
                    agent { label 'centos7' }
                    stages {
                        stage('cen7') {
                            steps {
                                dir('ws_centos7_x64') {
                                    deleteDir()
                                    unstash 'libcouchbase'
                                }
                            }
                        }
                        stage('build') {
                            post {
                                failure {
                                    sh('tar cf failure-ws_centos7_x64.tar ws_centos7_x64')
                                    archiveArtifacts(artifacts: "failure-ws_centos7_x64.tar", fingerprint: false)
                                }
                            }
                            steps {
                                dir('ws_centos7_x64') {
                                    dir('build') {
                                        sh('cmake -DCMAKE_BUILD_TYPE=RelWithDebInfo ../libcouchbase')
                                        sh("make -j8 ${VERBOSE.toBoolean() ? 'VERBOSE=1' : ''}")
                                        sh("make -j8 ${VERBOSE.toBoolean() ? 'VERBOSE=1' : ''} alltests")
                                    }
                                }
                                stash includes: 'ws_centos7_x64/', name: 'centos7_build'
                            }
                        }
                        stage('test') {
                            post {
                                failure {
                                    sh('tar cf failure-ws_centos7_x64.tar ws_centos7_x64')
                                    archiveArtifacts(artifacts: "failure-ws_centos7_x64.tar", fingerprint: false)
                                }
                                always {
                                    junit("ws_centos7_x64/build/*.xml")
                                }
                            }
                            steps {
                                dir('ws_centos7_x64/build') {
                                    sh("ctest ${VERBOSE.toBoolean() ? '-VV' : ''}")
                                }
                            }
                        }
                    }
                }
            }
        }
        stage('integration test') {
            when {
                expression {
                    return IS_GERRIT_TRIGGER.toBoolean() == false
                }
            }
            steps {
                script {
                    parallel doIntegrationStages(CLUSTER)
                }
            }
        }
        stage('package') {
            when {
                expression {
                    return IS_GERRIT_TRIGGER.toBoolean() == false
                }
            }
            parallel {
                stage('pkg centos7 x86_64') {
                    agent { label 'mock' }
                    stages {
                        stage('c64v7') {
                            steps {
                                dir('ws_centos64_v7') {
                                    sh("sudo chown couchbase:couchbase -R .")
                                    deleteDir()
                                    unstash 'libcouchbase'
                                }
                            }
                        }
                        stage('srpm') {
                            post {
                                failure {
                                    sh("tar cf failure-ws_centos64_v7.tar ws_centos64_v7")
                                    archiveArtifacts(artifacts: "failure-ws_centos64_v7.tar", fingerprint: false)
                                }
                            }
                            steps {
                                dir('ws_centos64_v7/build') {
                                    unstash 'tarball'
                                    sh("""
                                        sed 's/@VERSION@/${VERSION.rpmVer()}/g;s/@RELEASE@/${VERSION.rpmRel()}/g;s/@TARREDAS@/${VERSION.tarName()}/g' \
                                        < ../libcouchbase/packaging/rpm/libcouchbase.spec.in > libcouchbase.spec
                                    """.stripIndent())
                                    sh("""
                                        sudo mock --buildsrpm -r epel-7-x86_64 --spec libcouchbase.spec --sources ${pwd()} --old-chroot \
                                        --resultdir="libcouchbase-${VERSION.tar()}_centos7_srpm"
                                    """.stripIndent())
                                }
                            }
                        }
                        stage('rpm') {
                            post {
                                failure {
                                    sh("tar cf failure-ws_centos64_v7.tar ws_centos64_v7")
                                    archiveArtifacts(artifacts: "failure-ws_centos64_v7.tar", fingerprint: false)
                                }
                            }
                            steps {
                                dir('ws_centos64_v7/build') {
                                    sh("""
                                        sudo mock --rebuild -r epel-7-x86_64 --resultdir="libcouchbase-${VERSION.tar()}_centos7_x86_64" --old-chroot \
                                        libcouchbase-${VERSION.tar()}_centos7_srpm/libcouchbase-${VERSION.version()}-${VERSION.rpmRel()}.el7.src.rpm
                                    """.stripIndent())
                                    sh("sudo chown couchbase:couchbase -R libcouchbase-${VERSION.tar()}_centos7_x86_64")
                                    sh("rm -rf libcouchbase-${VERSION.tar()}_centos7_x86_64/*.log")
                                    sh("tar cf libcouchbase-${VERSION.tar()}_centos7_x86_64.tar libcouchbase-${VERSION.tar()}_centos7_x86_64")
                                    archiveArtifacts(artifacts: "libcouchbase-${VERSION.tar()}_centos7_x86_64.tar", fingerprint: true)
                                }
                            }
                        }
                    }
                }
                stage('pkg centos8 x86_64') {
                    agent { label 'mock' }
                    stages {
                        stage('c64v8') {
                            steps {
                                dir('ws_centos64_v8') {
                                    sh("sudo chown couchbase:couchbase -R .")
                                    deleteDir()
                                    unstash 'libcouchbase'
                                }
                            }
                        }
                        stage('srpm') {
                            post {
                                failure {
                                    sh("tar cf failure-ws_centos64_v8.tar ws_centos64_v8")
                                    archiveArtifacts(artifacts: "failure-ws_centos64_v8.tar", fingerprint: false)
                                }
                            }
                            steps {
                                dir('ws_centos64_v8/build') {
                                    unstash 'tarball'
                                    sh("""
                                        sed 's/@VERSION@/${VERSION.rpmVer()}/g;s/@RELEASE@/${VERSION.rpmRel()}/g;s/@TARREDAS@/${VERSION.tarName()}/g' \
                                        < ../libcouchbase/packaging/rpm/libcouchbase.spec.in > libcouchbase.spec
                                    """.stripIndent())
                                    sh("""
                                        sudo mock --buildsrpm -r epel-8-x86_64 --spec libcouchbase.spec --sources ${pwd()} --old-chroot \
                                        --resultdir="libcouchbase-${VERSION.tar()}_centos8_srpm"
                                    """.stripIndent())
                                }
                            }
                        }
                        stage('rpm') {
                            post {
                                failure {
                                    sh("tar cf failure-ws_centos64_v8.tar ws_centos64_v8")
                                    archiveArtifacts(artifacts: "failure-ws_centos64_v8.tar", fingerprint: false)
                                }
                            }
                            steps {
                                dir('ws_centos64_v8/build') {
                                    sh("""
                                        sudo mock --rebuild -r epel-8-x86_64 --resultdir="libcouchbase-${VERSION.tar()}_centos8_x86_64" --old-chroot \
                                        libcouchbase-${VERSION.tar()}_centos8_srpm/libcouchbase-${VERSION.version()}-${VERSION.rpmRel()}.el8.src.rpm
                                    """.stripIndent())
                                    sh("sudo chown couchbase:couchbase -R libcouchbase-${VERSION.tar()}_centos8_x86_64")
                                    sh("rm -rf libcouchbase-${VERSION.tar()}_centos8_x86_64/*.log")
                                    sh("tar cf libcouchbase-${VERSION.tar()}_centos8_x86_64.tar libcouchbase-${VERSION.tar()}_centos8_x86_64")
                                    archiveArtifacts(artifacts: "libcouchbase-${VERSION.tar()}_centos8_x86_64.tar", fingerprint: true)
                                    stash(includes: "libcouchbase-${VERSION.tar()}_centos8_x86_64/*.src.rpm", name: 'centos8-srpm')
                                }
                            }
                        }
                    }
                }
                    stage('pkg ubuntu2004 amd64') {
                        agent { label 'cowbuilder' }
                        stages {
                            stage('u64v20') {
                                steps {
                                    dir('ws_ubuntu2004_amd64') {
                                        sh("sudo chown couchbase:couchbase -R .")
                                        deleteDir()
                                        unstash 'libcouchbase'
                                    }
                                }
                            }
                            stage('cow1') {
                                when {
                                    expression {
                                        !fileExists("/var/cache/pbuilder/focal-amd64.cow/etc/os-release")
                                    }
                                }
                                steps {
                                    sh("""
                                        sudo apt-get install cowbuilder && \
                                        sudo cowbuilder --create \
                                        --basepath /var/cache/pbuilder/focal-amd64.cow \
                                        --distribution focal \
                                        --debootstrapopts --arch=amd64 \
                                        --components 'main universe' --mirror http://ftp.ubuntu.com/ubuntu --debootstrapopts --keyring=/usr/share/keyrings/ubuntu-archive-keyring.gpg
                                    """.stripIndent())
                                }
                            }
                            stage('cow2') {
                                when {
                                    expression {
                                        fileExists("/var/cache/pbuilder/focal-amd64.cow/etc/os-release")
                                    }
                                }
                                steps {
                                    sh('sudo cowbuilder --update --basepath /var/cache/pbuilder/focal-amd64.cow')
                                }
                            }
                            stage('src') {
                                post {
                                    failure {
                                        sh("tar cf failure-ws_ubuntu2004_amd64.tar ws_ubuntu2004_amd64")
                                        archiveArtifacts(artifacts: "failure-ws_ubuntu2004_amd64.tar", fingerprint: false)
                                    }
                                }
                                steps {
                                    dir('ws_ubuntu2004_amd64/build') {
                                        unstash 'tarball'
                                        sh("ln -s ${VERSION.tarName()}.tar.gz libcouchbase_${VERSION.deb()}.orig.tar.gz")
                                        sh("tar -xf ${VERSION.tarName()}.tar.gz")
                                        sh("cp -a ../libcouchbase/packaging/deb ${VERSION.tarName()}/debian")
                                        dir(VERSION.tarName()) {
                                            sh("""
                                                dch --no-auto-nmu --package libcouchbase --newversion ${VERSION.deb()}-1 \
                                                --create "Release package for libcouchbase ${VERSION.deb()}-1"
                                            """.stripIndent())
                                            sh("dpkg-buildpackage -rfakeroot -d -S -sa")
                                        }
                                    }
                                }
                            }
                            stage('deb') {
                                post {
                                    failure {
                                        sh("tar cf failure-ws_ubuntu2004_amd64.tar ws_ubuntu2004_amd64")
                                        archiveArtifacts(artifacts: "failure-ws_ubuntu2004_amd64.tar", fingerprint: false)
                                    }
                                }
                                steps {
                                    dir('ws_ubuntu2004_amd64/build') {
                                        sh("""
                                           sudo cowbuilder --build \
                                           --basepath /var/cache/pbuilder/focal-amd64.cow \
                                           --buildresult libcouchbase-${VERSION.deb()}_ubuntu2004_focal_amd64 \
                                           --debbuildopts -j8 \
                                           --debbuildopts "-us -uc" \
                                           libcouchbase_${VERSION.deb()}-1.dsc
                                        """.stripIndent())
                                        sh("sudo chown couchbase:couchbase -R libcouchbase-${VERSION.deb()}_ubuntu2004_focal_amd64")
                                        sh("tar cf libcouchbase-${VERSION.tar()}_ubuntu2004_focal_amd64.tar libcouchbase-${VERSION.deb()}_ubuntu2004_focal_amd64")
                                        archiveArtifacts(artifacts: "libcouchbase-${VERSION.tar()}_ubuntu2004_focal_amd64.tar", fingerprint: true)
                                    }
                                }
                            }
                        }
                    }
                    stage('pkg ubuntu1804 amd64') {
                        agent { label 'cowbuilder' }
                        stages {
                            stage('u64v18') {
                                steps {
                                    dir('ws_ubuntu1804_amd64') {
                                        sh("sudo chown couchbase:couchbase -R .")
                                        deleteDir()
                                        unstash 'libcouchbase'
                                    }
                                }
                            }
                            stage('cow1') {
                                when {
                                    expression {
                                        !fileExists("/var/cache/pbuilder/bionic-amd64.cow/etc/os-release")
                                    }
                                }
                                steps {
                                    sh("""
                                        sudo apt-get install cowbuilder && \
                                        sudo cowbuilder --create \
                                        --basepath /var/cache/pbuilder/bionic-amd64.cow \
                                        --distribution bionic \
                                        --debootstrapopts --arch=amd64 \
                                        --components 'main universe' --mirror http://ftp.ubuntu.com/ubuntu --debootstrapopts --keyring=/usr/share/keyrings/ubuntu-archive-keyring.gpg
                                    """.stripIndent())
                                }
                            }
                            stage('cow2') {
                                when {
                                    expression {
                                        fileExists("/var/cache/pbuilder/bionic-amd64.cow/etc/os-release")
                                    }
                                }
                                steps {
                                    sh('sudo cowbuilder --update --basepath /var/cache/pbuilder/bionic-amd64.cow')
                                }
                            }
                            stage('src') {
                                post {
                                    failure {
                                        sh("tar cf failure-ws_ubuntu1804_amd64.tar ws_ubuntu1804_amd64")
                                        archiveArtifacts(artifacts: "failure-ws_ubuntu1804_amd64.tar", fingerprint: false)
                                    }
                                }
                                steps {
                                    dir('ws_ubuntu1804_amd64/build') {
                                        unstash 'tarball'
                                        sh("ln -s ${VERSION.tarName()}.tar.gz libcouchbase_${VERSION.deb()}.orig.tar.gz")
                                        sh("tar -xf ${VERSION.tarName()}.tar.gz")
                                        sh("cp -a ../libcouchbase/packaging/deb ${VERSION.tarName()}/debian")
                                        dir(VERSION.tarName()) {
                                            sh("""
                                                dch --no-auto-nmu --package libcouchbase --newversion ${VERSION.deb()}-1 \
                                                --create "Release package for libcouchbase ${VERSION.deb()}-1"
                                            """.stripIndent())
                                            sh("dpkg-buildpackage -rfakeroot -d -S -sa")
                                        }
                                    }
                                }
                            }
                            stage('deb') {
                                post {
                                    failure {
                                        sh("tar cf failure-ws_ubuntu1804_amd64.tar ws_ubuntu1804_amd64")
                                        archiveArtifacts(artifacts: "failure-ws_ubuntu1804_amd64.tar", fingerprint: false)
                                    }
                                }
                                steps {
                                    dir('ws_ubuntu1804_amd64/build') {
                                        sh("""
                                           sudo cowbuilder --build \
                                           --basepath /var/cache/pbuilder/bionic-amd64.cow \
                                           --buildresult libcouchbase-${VERSION.deb()}_ubuntu1804_bionic_amd64 \
                                           --debbuildopts -j8 \
                                           --debbuildopts "-us -uc" \
                                           libcouchbase_${VERSION.deb()}-1.dsc
                                        """.stripIndent())
                                        sh("sudo chown couchbase:couchbase -R libcouchbase-${VERSION.deb()}_ubuntu1804_bionic_amd64")
                                        sh("tar cf libcouchbase-${VERSION.tar()}_ubuntu1804_bionic_amd64.tar libcouchbase-${VERSION.deb()}_ubuntu1804_bionic_amd64")
                                        archiveArtifacts(artifacts: "libcouchbase-${VERSION.tar()}_ubuntu1804_bionic_amd64.tar", fingerprint: true)
                                    }
                                }
                            }
                        }
                    }
                    stage('pkg ubuntu1604 amd64') {
                        agent { label 'cowbuilder' }
                        stages {
                            stage('u64v16') {
                                steps {
                                    dir('ws_ubuntu1604_amd64') {
                                        sh("sudo chown couchbase:couchbase -R .")
                                        deleteDir()
                                        unstash 'libcouchbase'
                                    }
                                }
                            }
                            stage('cow1') {
                                when {
                                    expression {
                                        !fileExists("/var/cache/pbuilder/xenial-amd64.cow/etc/os-release")
                                    }
                                }
                                steps {
                                    sh("""
                                        sudo apt-get install cowbuilder && \
                                        sudo cowbuilder --create \
                                        --basepath /var/cache/pbuilder/xenial-amd64.cow \
                                        --distribution xenial \
                                        --debootstrapopts --arch=amd64 \
                                        --components 'main universe' --mirror http://ftp.ubuntu.com/ubuntu --debootstrapopts --keyring=/usr/share/keyrings/ubuntu-archive-keyring.gpg
                                    """.stripIndent())
                                }
                            }
                            stage('cow2') {
                                when {
                                    expression {
                                        fileExists("/var/cache/pbuilder/xenial-amd64.cow/etc/os-release")
                                    }
                                }
                                steps {
                                    sh('sudo cowbuilder --update --basepath /var/cache/pbuilder/xenial-amd64.cow')
                                }
                            }
                            stage('src') {
                                post {
                                    failure {
                                        sh("tar cf failure-ws_ubuntu1604_amd64.tar ws_ubuntu1604_amd64")
                                        archiveArtifacts(artifacts: "failure-ws_ubuntu1604_amd64.tar", fingerprint: false)
                                    }
                                }
                                steps {
                                    dir('ws_ubuntu1604_amd64/build') {
                                        unstash 'tarball'
                                        sh("ln -s ${VERSION.tarName()}.tar.gz libcouchbase_${VERSION.deb()}.orig.tar.gz")
                                        sh("tar -xf ${VERSION.tarName()}.tar.gz")
                                        sh("cp -a ../libcouchbase/packaging/deb ${VERSION.tarName()}/debian")
                                        dir(VERSION.tarName()) {
                                            sh("""
                                                dch --no-auto-nmu --package libcouchbase --newversion ${VERSION.deb()}-1 \
                                                --create "Release package for libcouchbase ${VERSION.deb()}-1"
                                            """.stripIndent())
                                            sh("dpkg-buildpackage -rfakeroot -d -S -sa")
                                        }
                                    }
                                }
                            }
                            stage('deb') {
                                post {
                                    failure {
                                        sh("tar cf failure-ws_ubuntu1604_amd64.tar ws_ubuntu1604_amd64")
                                        archiveArtifacts(artifacts: "failure-ws_ubuntu1604_amd64.tar", fingerprint: false)
                                    }
                                }
                                steps {
                                    dir('ws_ubuntu1604_amd64/build') {
                                        sh("""
                                           sudo cowbuilder --build \
                                           --basepath /var/cache/pbuilder/xenial-amd64.cow \
                                           --buildresult libcouchbase-${VERSION.deb()}_ubuntu1604_xenial_amd64 \
                                           --debbuildopts -j8 \
                                           --debbuildopts "-us -uc" \
                                           libcouchbase_${VERSION.deb()}-1.dsc
                                        """.stripIndent())
                                        sh("sudo chown couchbase:couchbase -R libcouchbase-${VERSION.deb()}_ubuntu1604_xenial_amd64")
                                        sh("tar cf libcouchbase-${VERSION.tar()}_ubuntu1604_xenial_amd64.tar libcouchbase-${VERSION.deb()}_ubuntu1604_xenial_amd64")
                                        archiveArtifacts(artifacts: "libcouchbase-${VERSION.tar()}_ubuntu1604_xenial_amd64.tar", fingerprint: true)
                                    }
                                }
                            }
                        }
                    }
                    stage('pkg debian9 amd64') {
                        agent { label 'cowbuilder' }
                        stages {
                            stage('d64v9') {
                                steps {
                                    dir('ws_debian9_amd64') {
                                        sh("sudo chown couchbase:couchbase -R .")
                                        deleteDir()
                                        unstash 'libcouchbase'
                                    }
                                }
                            }
                            stage('cow1') {
                                when {
                                    expression {
                                        !fileExists("/var/cache/pbuilder/stretch-amd64.cow/etc/os-release")
                                    }
                                }
                                steps {
                                    sh("""
                                        sudo apt-get install cowbuilder && \
                                        sudo cowbuilder --create \
                                        --basepath /var/cache/pbuilder/stretch-amd64.cow \
                                        --distribution stretch \
                                        --debootstrapopts --arch=amd64 \
                                         --components 'main'
                                    """.stripIndent())
                                }
                            }
                            stage('cow2') {
                                when {
                                    expression {
                                        fileExists("/var/cache/pbuilder/stretch-amd64.cow/etc/os-release")
                                    }
                                }
                                steps {
                                    sh('sudo cowbuilder --update --basepath /var/cache/pbuilder/stretch-amd64.cow')
                                }
                            }
                            stage('src') {
                                post {
                                    failure {
                                        sh("tar cf failure-ws_debian9_amd64.tar ws_debian9_amd64")
                                        archiveArtifacts(artifacts: "failure-ws_debian9_amd64.tar", fingerprint: false)
                                    }
                                }
                                steps {
                                    dir('ws_debian9_amd64/build') {
                                        unstash 'tarball'
                                        sh("ln -s ${VERSION.tarName()}.tar.gz libcouchbase_${VERSION.deb()}.orig.tar.gz")
                                        sh("tar -xf ${VERSION.tarName()}.tar.gz")
                                        sh("cp -a ../libcouchbase/packaging/deb ${VERSION.tarName()}/debian")
                                        dir(VERSION.tarName()) {
                                            sh("""
                                                dch --no-auto-nmu --package libcouchbase --newversion ${VERSION.deb()}-1 \
                                                --create "Release package for libcouchbase ${VERSION.deb()}-1"
                                            """.stripIndent())
                                            sh("dpkg-buildpackage -rfakeroot -d -S -sa")
                                        }
                                    }
                                }
                            }
                            stage('deb') {
                                post {
                                    failure {
                                        sh("tar cf failure-ws_debian9_amd64.tar ws_debian9_amd64")
                                        archiveArtifacts(artifacts: "failure-ws_debian9_amd64.tar", fingerprint: false)
                                    }
                                }
                                steps {
                                    dir('ws_debian9_amd64/build') {
                                        sh("""
                                           sudo cowbuilder --build \
                                           --basepath /var/cache/pbuilder/stretch-amd64.cow \
                                           --buildresult libcouchbase-${VERSION.deb()}_debian9_stretch_amd64 \
                                           --debbuildopts -j8 \
                                           --debbuildopts "-us -uc" \
                                           libcouchbase_${VERSION.deb()}-1.dsc
                                        """.stripIndent())
                                        sh("sudo chown couchbase:couchbase -R libcouchbase-${VERSION.deb()}_debian9_stretch_amd64")
                                        sh("tar cf libcouchbase-${VERSION.tar()}_debian9_stretch_amd64.tar libcouchbase-${VERSION.deb()}_debian9_stretch_amd64")
                                        archiveArtifacts(artifacts: "libcouchbase-${VERSION.tar()}_debian9_stretch_amd64.tar", fingerprint: true)
                                    }
                                }
                            }
                        }
                    }
                    stage('pkg debian10 amd64') {
                        agent { label 'cowbuilder' }
                        stages {
                            stage('d64v10') {
                                steps {
                                    dir('ws_debian10_amd64') {
                                        sh("sudo chown couchbase:couchbase -R .")
                                        deleteDir()
                                        unstash 'libcouchbase'
                                    }
                                }
                            }
                            stage('cow1') {
                                when {
                                    expression {
                                        !fileExists("/var/cache/pbuilder/buster-amd64.cow/etc/os-release")
                                    }
                                }
                                steps {
                                    sh("""
                                        sudo apt-get install cowbuilder && \
                                        sudo cowbuilder --create \
                                        --basepath /var/cache/pbuilder/buster-amd64.cow \
                                        --distribution buster \
                                        --debootstrapopts --arch=amd64 \
                                         --components 'main'
                                    """.stripIndent())
                                }
                            }
                            stage('cow2') {
                                when {
                                    expression {
                                        fileExists("/var/cache/pbuilder/buster-amd64.cow/etc/os-release")
                                    }
                                }
                                steps {
                                    sh('sudo cowbuilder --update --basepath /var/cache/pbuilder/buster-amd64.cow')
                                }
                            }
                            stage('src') {
                                post {
                                    failure {
                                        sh("tar cf failure-ws_debian10_amd64.tar ws_debian10_amd64")
                                        archiveArtifacts(artifacts: "failure-ws_debian10_amd64.tar", fingerprint: false)
                                    }
                                }
                                steps {
                                    dir('ws_debian10_amd64/build') {
                                        unstash 'tarball'
                                        sh("ln -s ${VERSION.tarName()}.tar.gz libcouchbase_${VERSION.deb()}.orig.tar.gz")
                                        sh("tar -xf ${VERSION.tarName()}.tar.gz")
                                        sh("cp -a ../libcouchbase/packaging/deb ${VERSION.tarName()}/debian")
                                        dir(VERSION.tarName()) {
                                            sh("""
                                                dch --no-auto-nmu --package libcouchbase --newversion ${VERSION.deb()}-1 \
                                                --create "Release package for libcouchbase ${VERSION.deb()}-1"
                                            """.stripIndent())
                                            sh("dpkg-buildpackage -rfakeroot -d -S -sa")
                                        }
                                    }
                                }
                            }
                            stage('deb') {
                                post {
                                    failure {
                                        sh("tar cf failure-ws_debian10_amd64.tar ws_debian10_amd64")
                                        archiveArtifacts(artifacts: "failure-ws_debian10_amd64.tar", fingerprint: false)
                                    }
                                }
                                steps {
                                    dir('ws_debian10_amd64/build') {
                                        sh("""
                                           sudo cowbuilder --build \
                                           --basepath /var/cache/pbuilder/buster-amd64.cow \
                                           --buildresult libcouchbase-${VERSION.deb()}_debian10_buster_amd64 \
                                           --debbuildopts -j8 \
                                           --debbuildopts "-us -uc" \
                                           libcouchbase_${VERSION.deb()}-1.dsc
                                        """.stripIndent())
                                        sh("sudo chown couchbase:couchbase -R libcouchbase-${VERSION.deb()}_debian10_buster_amd64")
                                        sh("tar cf libcouchbase-${VERSION.tar()}_debian10_buster_amd64.tar libcouchbase-${VERSION.deb()}_debian10_buster_amd64")
                                        archiveArtifacts(artifacts: "libcouchbase-${VERSION.tar()}_debian10_buster_amd64.tar", fingerprint: true)
                                    }
                                }
                            }
                        }
                    }
            }
        }
        stage('amzn2') {
            agent { label 'amzn2' }
            steps {
                sh('sudo yum install -y rpm-build yum-utils')
                cleanWs()
                unstash('centos8-srpm')
                sh('sudo yum-builddep -y libcouchbase-*/*.src.rpm')
                sh('rpmbuild --rebuild libcouchbase-*/*.src.rpm -D "_rpmdir output"')
                dir('output') {
                    sh("mv x86_64 libcouchbase-${VERSION.tar()}_amzn2_x86_64")
                    sh("tar cf libcouchbase-${VERSION.tar()}_amzn2_x86_64.tar libcouchbase-${VERSION.tar()}_amzn2_x86_64")
                    archiveArtifacts(artifacts: "libcouchbase-${VERSION.tar()}_amzn2_x86_64.tar", fingerprint: true)
                }
            }
        }
        stage('repositories') {
            when {
                expression {
                    return IS_GERRIT_TRIGGER.toBoolean() == false
                }
            }
            agent none
            steps {
                build(job: 'lcb-repo-pipeline')
            }
        }
    }
}
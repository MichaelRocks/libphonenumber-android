#!/usr/bin/env bash
set -e

format_version() {
  local parts=("$@")
  local version=
  printf -v version "%s." "${parts[@]}"
  version=${version%?}
  echo ${version}
}

find_next_version() {
  local next_parts=("$@")
  for (( index=${#next_parts[@]}-1 ; index>=0 ; index-- )) ; do
    ((next_parts[index]++))
    local next_version=$(format_version ${next_parts[@]})
    if [[ $(git tag -l "v$next_version") ]]; then
      echo ${next_version}
      return 0
    else
      next_parts[index]=0
    fi
  done
  return 1
}

LOCAL="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
REMOTE=$1
BASE_TRAP="cd ${LOCAL} > /dev/null; git merge -q --abort; git checkout -q develop"

trap "${BASE_TRAP}; exit" INT TERM EXIT

echo "Merging changes from '${REMOTE}' to '${LOCAL}'"

echo "Updating the local repo..."
git checkout -q develop
git pull -q > /dev/null

VERSION="$( sed -ne 's/^[[:space:]]*version[[:space:]]=[[:space:]]'"'"'\([0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*\)\(\-[0-9][0-9]*\)\{0,1\}'"'"'[[:space:]]*$/\1/p' ${LOCAL}/build.gradle)"
if [[ -z "$VERSION" ]]; then
  echo "Current version is not found"
  exit 1
fi

PARTS=( ${VERSION//./ } )

echo "Updating the remote repo..."
pushd ${REMOTE} > /dev/null
git checkout -q master
git pull -q > /dev/null
git fetch -q --tags
NEXT_VERSION=$(find_next_version ${PARTS[@]})
popd > /dev/null

echo "Current version is ${VERSION}"
if [[ -z NEXT_VERSION ]]; then
  echo "Next version is not found"
  exit 1
fi
echo "Next version is ${NEXT_VERSION}"

echo "Creating a release branch..."
git checkout -q -b "release/${NEXT_VERSION}"
trap "${BASE_TRAP}; git branch -D "release/${NEXT_VERSION}"; exit" INT TERM EXIT

sed -i '.tmp' "s/${VERSION}/${NEXT_VERSION}/g" build.gradle
sed -i '.tmp' "s/${VERSION}/${NEXT_VERSION}/g" README.md
git add build.gradle > /dev/null
git add README.md > /dev/null
rm build.gradle.tmp > /dev/null
rm README.md.tmp > /dev/null
git commit -q -m "Bump version to ${NEXT_VERSION}"

CURRENT_TAG="v${VERSION}"
NEXT_TAG="v${NEXT_VERSION}"
PATCH_PATH="${LOCAL}/${NEXT_TAG}.patch"

echo "Merging code changes..."

pushd ${REMOTE} > /dev/null
git diff ${CURRENT_TAG} ${NEXT_TAG} -- java/libphonenumber/**/*.java > ${PATCH_PATH}
popd > /dev/null

sed -i '.tmp' -n \
  -e 's:java/libphonenumber/src/com/google/i18n/phonenumbers:library/src/main/java/io/michaelrocks/libphonenumber/android:g' \
  -e 's:java/libphonenumber/test/com/google/i18n/phonenumbers:library/src/test/java/io/michaelrocks/libphonenumber/android:g' \
  ${PATCH_PATH}

patch -p1 < ${PATCH_PATH}

rm "${PATCH_PATH}" > /dev/null
rm "${PATCH_PATH}.tmp" > /dev/null
find ${LOCAL} -name *.orig -delete

REJ_COUNT=`find ${LOCAL} -name *.rej | wc -l`
if [[ ${REJ_COUNT} != 0 ]]; then
  echo 'Seems a patch cannot be applied manually'
  exit 1
fi

echo "Merging metadata changes..."

pushd ${REMOTE} > /dev/null
git checkout -q ${NEXT_TAG}
popd > /dev/null

cp "${REMOTE}"/java/libphonenumber/src/com/google/i18n/phonenumbers/data/* "${LOCAL}"/library/src/main/assets/io/michaelrocks/libphonenumber/android/data
cp "${REMOTE}"/java/libphonenumber/src/com/google/i18n/phonenumbers/data/* "${LOCAL}"/library/src/test/resources/io/michaelrocks/libphonenumber/android/data
cp "${REMOTE}"/java/libphonenumber/test/com/google/i18n/phonenumbers/data/* "${LOCAL}"/library/src/test/resources/io/michaelrocks/libphonenumber/android/data

git commit -q -a -m "Merge code and metadata changes from ${NEXT_VERSION}"

git checkout -q develop
git pull -q > /dev/null
git merge -q --no-ff --no-edit "release/${NEXT_VERSION}"

git checkout -q master
git pull -q > /dev/null
git merge -q --no-ff --no-edit "release/${NEXT_VERSION}"

git tag "v${NEXT_VERSION}" > /dev/null
git branch -q -d "release/${NEXT_VERSION}"

git checkout -q "v${NEXT_VERSION}"
./gradlew clean build
./gradlew connectedCheck
./gradlew bintrayUploadRelease

git checkout -q develop
git push -q
git checkout -q master
git push -q
git push -q --tags

git checkout -q develop

trap - INT TERM EXIT

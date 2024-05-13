const fileInput = document.getElementById('customFile');
const generateDiv = document.getElementById('generating')

fileInput.addEventListener('change', handleFileSelect);

const charCheckbox = document.getElementById('charCheckbox')
const rpdCheckbox = document.getElementById('rpdCheckbox')
const fosCheckbox = document.getElementById('fosCheckbox')
const generateBtn = document.getElementById('generateBtn')
syllabusFile = null

charCheckbox.addEventListener('change', checkSubmitButton);
rpdCheckbox.addEventListener('change', checkSubmitButton);
fosCheckbox.addEventListener('change', checkSubmitButton);
fileInput.addEventListener('change', checkSubmitButton);

function checkSubmitButton(){
    let charCheckboxState = $('#charCheckbox').is(":checked")
    let rpdCheckboxState = $('#rpdCheckbox').is(":checked")
    let fosCheckboxState = $('#fosCheckbox').is(":checked")
    if(syllabusFile && (charCheckboxState || rpdCheckboxState || fosCheckboxState)){
        generateBtn.classList.remove('disabled')
    }
    else{
        generateBtn.classList.add('disabled')
    }
}
function handleFileSelect(event) {
    syllabusFile = event.target.files[0];
    $('label[for="customFile"]').html(syllabusFile.name)
}

function generate(){
    generateDiv.style.removeProperty('display')

    const formData = new FormData();
    let charCheckboxState = $('#charCheckbox').is(":checked")
    let rpdCheckboxState = $('#rpdCheckbox').is(":checked")
    let fosCheckboxState = $('#fosCheckbox').is(":checked")

    formData.append('file', syllabusFile);
    formData.append('charCheckbox', charCheckboxState)
    formData.append('rpdCheckbox', rpdCheckboxState)
    formData.append('fosCheckbox', fosCheckboxState)

    fetch("http://localhost:8080/opop/generate", {
        method: 'POST',
        body: formData
    })
    .then(resp => resp.status === 200 ? resp.blob() : Promise.reject('Что то пошло не так'))
    .then(blob => {
        generateDiv.style.cssText = "display: none !important;";
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.style.display = 'none';
        a.href = url;
        // the filename you want
        a.download = 'ОПОП.zip';
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);

        $('label[for="customFile"]').html('Загрузите учебный план(xls/xlsx)')
        syllabusFile = null
     })
    .catch(function (error) {
        console.log('error', error)
        $('label[for="customFile"]').html('Загрузите учебный план(xls/xlsx)')
        syllabusFile = null
    })
}
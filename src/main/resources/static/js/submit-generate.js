const fileInput = document.getElementById('customFile');
const generateDiv = document.getElementById('generating')

const mainDiv = document.getElementById('main')
let errorHdiv = null
let mainBtnGroup = null

const charCheckbox = document.getElementById('charCheckbox')
const rpdCheckbox = document.getElementById('rpdCheckbox')
const fosCheckbox = document.getElementById('fosCheckbox')
const generateBtn = document.getElementById('generateBtn')
syllabusFile = null

charCheckbox.addEventListener('change', checkSubmitButton);
rpdCheckbox.addEventListener('change', checkSubmitButton);
fosCheckbox.addEventListener('change', checkSubmitButton);
fileInput.addEventListener('change', checkSubmitButton);

function createCustomLabel(text){
    errorHdiv = document.createElement('div')
    errorHdiv.className = 'col-sm-6'
    errorHdiv.style = 'margin-block: 10px'

    let ok = document.createElement('h4')
    ok.innerText = text
    errorHdiv.append(ok)

    let btnGroup = document.createElement('div')
    btnGroup.className = 'btn-group-vertical'

    mainDiv.append(errorHdiv)
    mainDiv.append(btnGroup)
    mainBtnGroup = btnGroup
}

function checkSubmitButton(event){
    if(event.target.files){
        handleFileSelect(event)
    }
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
    if(errorHdiv){
        mainDiv.removeChild(errorHdiv)
        mainDiv.removeChild(mainBtnGroup)
        errorHdiv = null
    }
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
    }).then((response) => {
        if (response.ok) {
            return response.blob();
        }
        return response.text().then(text => { throw new Error(text) })

    })
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
        generateBtn.classList.add('disabled')
        syllabusFile = null
        fileInput.value = null
     })
    .catch(function (error) {
        $('label[for="customFile"]').html('Загрузите учебный план(xls/xlsx)')
        generateDiv.style.cssText = "display: none !important;";
        generateBtn.classList.add('disabled')
        syllabusFile = null
        fileInput.value = null
        createCustomLabel(error.message)
    })
}
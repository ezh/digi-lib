" Vim syntax file
" " Language: Digimead log files
" " Maintainer: Alexey Aksenov
" " Latest Revision: 25 January 2014

syn match DATE /^\d\{4}-\d\{2}-\d\{2} \d\{2}:\d\{2}:\d\{2}\.\d\{3}.\{1}\d\{4} / contained nextgroup=PIDTID skipwhite
syn match PIDTID /\(\[.\{-}\]\|P\S\{5} T\S\{5}\)/ contained nextgroup=LEVELT,LEVELTRACE,LEVELD,LEVELDEBUG,LEVELI,LEVELINFO,LEVELW,LEVELWARN,LEVELE,LEVELERROR skipwhite

syn keyword LEVELT T contained nextgroup=CLASST skipwhite
syn keyword LEVELTRACE TRACE contained nextgroup=CLASST skipwhite
syn match CLASST /@[^ ]*/ contained nextgroup=TRACE skipwhite
syn match TRACE /.*/ contained

syn keyword LEVELD D contained nextgroup=CLASSD skipwhite
syn keyword LEVELDEBUG DEBUG contained nextgroup=CLASSD skipwhite
syn match CLASSD /@[^ ]*/ contained nextgroup=DEBUG skipwhite
syn match DEBUG /.*/ contained

syn keyword LEVELI I contained nextgroup=CLASSI skipwhite
syn keyword LEVELINFO INFO contained nextgroup=CLASSI skipwhite
syn match CLASSI /@[^ ]*/ contained nextgroup=INFO skipwhite
syn match INFO /.*/ contained

syn keyword LEVELW W contained nextgroup=CLASSW skipwhite
syn keyword LEVELWARN WARN contained nextgroup=CLASSW skipwhite
syn match CLASSW /@[^ ]*/ contained nextgroup=WARNING skipwhite
syn match WARNING /.*/ contained

syn keyword LEVELE E contained nextgroup=CLASSE skipwhite
syn keyword LEVELERROR ERROR contained nextgroup=CLASSE skipwhite
syn match CLASSE /@[^ ]*/ contained nextgroup=ERROR skipwhite
syn match ERROR /.*/ contained

syn region LogLine start=/^\d\{4}-\d\{2}-\d\{2} \d\{2}:\d\{2}:\d\{2}\.\d\{3}.\{1}\d\{4} \(\[.\{-}\]\|P\S\{5} T\S\{5}\)\s\@=/ end=/$/ contains=DATE

hi link DATE NonText
hi link PIDTID SignColumn
hi link CLASS Function

hi link LEVELT Comment
hi link TRACE Comment

hi link LEVELD Statement
hi link DEBUG Statement

hi link LEVELW Directory
hi link WARNING Directory

hi link LEVELE WarningMsg
hi link ERROR WarningMsg
